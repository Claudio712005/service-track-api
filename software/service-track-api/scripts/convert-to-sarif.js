#!/usr/bin/env node
"use strict";

const fs = require("fs");
const path = require("path");

const reportsDir = process.argv[2] ?? "reports";
const inputPath  = path.join(reportsDir, "dependency-check-report.json");
const outputPath = path.join(reportsDir, "dependency-check.sarif");

if (!fs.existsSync(inputPath)) {
    console.error(`ERROR: input not found: ${inputPath}`);
    process.exit(1);
}

const severityToLevel = (s = "") => {
    switch (s.toUpperCase()) {
        case "CRITICAL":
        case "HIGH":     return "error";
        case "MEDIUM":   return "warning";
        case "LOW":      return "note";
        default:         return "none";
    }
};

const report = JSON.parse(fs.readFileSync(inputPath, "utf8"));
const dependencies = report.dependencies ?? [];

const rulesMap = new Map();
const results  = [];

for (const dep of dependencies) {
    for (const vuln of dep.vulnerabilities ?? []) {
        const ruleId = vuln.name;

        if (!rulesMap.has(ruleId)) {
            rulesMap.set(ruleId, {
                id:               ruleId,
                name:             ruleId,
                shortDescription: { text: vuln.name },
                fullDescription:  { text: vuln.description ?? vuln.name },
                helpUri:          vuln.references?.[0]?.url ?? `https://nvd.nist.gov/vuln/detail/${ruleId}`,
                defaultConfiguration: {
                    level: severityToLevel(vuln.severity),
                },
                properties: {
                    tags: ["security", "sca"],
                },
            });
        }

        results.push({
            ruleId,
            level: severityToLevel(vuln.severity),
            message: {
                text: `${dep.fileName}: ${vuln.name} (${vuln.severity ?? "UNKNOWN"}) — ${vuln.description ?? "no description"}`,
            },
            locations: [
                {
                    physicalLocation: {
                        artifactLocation: {
                            uri:       dep.filePath ?? dep.fileName ?? "unknown",
                            uriBaseId: "%SRCROOT%",
                        },
                    },
                },
            ],
            partialFingerprints: {
                "primaryLocationLineHash/v1": `${ruleId}:${dep.fileName}`,
            },
        });
    }
}

const sarif = {
    $schema: "https://raw.githubusercontent.com/oasis-tcs/sarif-spec/master/Schemata/sarif-schema-2.1.0.json",
    version: "2.1.0",
    runs: [
        {
            tool: {
                driver: {
                    name:            "OWASP Dependency Check",
                    version:         report.reportSchema ?? "unknown",
                    informationUri:  "https://owasp.org/www-project-dependency-check/",
                    rules:           Array.from(rulesMap.values()),
                },
            },
            results,
        },
    ],
};

fs.writeFileSync(outputPath, JSON.stringify(sarif, null, 2), "utf8");
console.log(`SARIF written → ${outputPath} (${results.length} result(s))`);
