const fs = require("fs");

const input = JSON.parse(fs.readFileSync("reports/dependency-check-report.json"));

const sarif = {
    version: "2.1.0",
    runs: [
        {
            tool: {
                driver: {
                    name: "Dependency Check"
                }
            },
            results: []
        }
    ]
};

input.dependencies.forEach(dep => {
    (dep.vulnerabilities || []).forEach(vuln => {
        sarif.runs[0].results.push({
            ruleId: vuln.name,
            level: vuln.severity.toLowerCase(),
            message: {
                text: vuln.description
            },
            locations: [
                {
                    physicalLocation: {
                        artifactLocation: {
                            uri: dep.fileName
                        }
                    }
                }
            ]
        });
    });
});

fs.writeFileSync("dependency-check.sarif", JSON.stringify(sarif, null, 2));