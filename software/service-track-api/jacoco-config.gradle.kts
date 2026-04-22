import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.gradle.api.tasks.testing.Test
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

fun printCoverageReport(
    layerName: String,
    instrRatio: Double,
    xmlReport: File,
    branchRatio: Double = instrRatio
) {
    val hr = "━".repeat(70)

    fun explicacao(tipo: String) = when (tipo) {
        "INSTRUCTION" -> "Instruções executadas (nível mais granular)"
        "BRANCH" -> "Decisões lógicas (if/else, when, etc)"
        "LINE" -> "Linhas de código executadas"
        else -> ""
    }

    if (!xmlReport.exists()) {
        println("\n$hr")
        println("📊 RELATÓRIO DE COBERTURA — $layerName")
        println(hr)
        println("⚠️  Arquivo XML não encontrado.")
        println("👉 Execute: ./gradlew test jacocoTestReport")
        println("$hr\n")
        return
    }

    val dbf = DocumentBuilderFactory.newInstance().apply {
        isValidating = false
        setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        setFeature("http://xml.org/sax/features/external-general-entities", false)
        setFeature("http://xml.org/sax/features/external-parameter-entities", false)
    }

    val root = dbf.newDocumentBuilder().parse(xmlReport).documentElement

    var instrCov = 0; var instrMis = 0
    var branchCov = 0; var branchMis = 0
    var lineCov = 0; var lineMis = 0

    val nodes = root.childNodes
    for (i in 0 until nodes.length) {
        val node = nodes.item(i)
        if (node.nodeName != "counter") continue

        val el = node as Element
        val cov = el.getAttribute("covered").toIntOrNull() ?: 0
        val mis = el.getAttribute("missed").toIntOrNull() ?: 0

        when (el.getAttribute("type")) {
            "INSTRUCTION" -> { instrCov = cov; instrMis = mis }
            "BRANCH" -> { branchCov = cov; branchMis = mis }
            "LINE" -> { lineCov = cov; lineMis = mis }
        }
    }

    fun percentual(cov: Int, mis: Int): Double =
        if (cov + mis == 0) 100.0 else cov.toDouble() / (cov + mis) * 100.0

    fun passou(cov: Int, mis: Int, ratio: Double): Boolean =
        percentual(cov, mis) / 100.0 >= ratio

    fun linha(nome: String, tipo: String, cov: Int, mis: Int, ratio: Double): String {
        val pct = percentual(cov, mis)
        val total = cov + mis
        val minLabel = if (ratio > 0.0) " (mín: ${"%.0f".format(ratio * 100)}%)" else " (não verificado)"
        val status = when {
            ratio <= 0.0 -> "ℹ️  Informativo"
            passou(cov, mis, ratio) -> "✅ OK"
            else -> "❌ Abaixo do mínimo"
        }
        return """
  $nome:
     ➤ Cobertura: ${"%.2f".format(pct)}%$minLabel
     ➤ Executado: $cov de $total
     ➤ Tipo: ${explicacao(tipo)}
     ➤ Status: $status
""".trimIndent()
    }

    val instrPass = passou(instrCov, instrMis, instrRatio)
    val branchPass = branchRatio <= 0.0 || passou(branchCov, branchMis, branchRatio)

    val resultadoFinal =
        if (instrPass && branchPass) "✅ APROVADO"
        else "❌ REPROVADO"

    println()
    println(hr)
    println("📊 RELATÓRIO DE COBERTURA — $layerName")
    println("🎯 Instruções mínimas exigidas: ${"%.0f%%".format(instrRatio * 100)}")
    println(hr)

    println(linha("🧠 Instruções", "INSTRUCTION", instrCov, instrMis, instrRatio))
    println(linha("🔀 Decisões (Branches)", "BRANCH", branchCov, branchMis, branchRatio))
    println(linha("📏 Linhas", "LINE", lineCov, lineMis, instrRatio))

    println(hr)
    println("🏁 RESULTADO FINAL: $resultadoFinal")
    println(hr)
    println()
}

subprojects {
    apply(plugin = "jacoco")

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        finalizedBy(tasks.withType<JacocoReport>())
    }

    tasks.withType<JacocoReport>().configureEach {
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }
}

project(":_domain").afterEvaluate {
    tasks.named<JacocoReport>("jacocoTestReport") {
        doLast {
            printCoverageReport("Domain", 0.90, reports.xml.outputLocation.get().asFile, branchRatio = 0.90)
        }
    }

    tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
        dependsOn(tasks.named("test"))
        violationRules {
            rule {
                limit {
                    counter = "INSTRUCTION"
                    value = "COVEREDRATIO"
                    minimum = "0.90".toBigDecimal()
                }
            }
            rule {
                limit {
                    counter = "BRANCH"
                    value = "COVEREDRATIO"
                    minimum = "0.90".toBigDecimal()
                }
            }
        }
    }

    tasks.named("check") {
        dependsOn("jacocoTestCoverageVerification")
    }
}

project(":_application").afterEvaluate {
    tasks.named<JacocoReport>("jacocoTestReport") {
        doLast {
            printCoverageReport("Application", 0.80, reports.xml.outputLocation.get().asFile, branchRatio = 0.80)
        }
    }

    tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
        dependsOn(tasks.named("test"))
        violationRules {
            rule {
                limit {
                    counter = "INSTRUCTION"
                    value = "COVEREDRATIO"
                    minimum = "0.80".toBigDecimal()
                }
            }
            rule {
                limit {
                    counter = "BRANCH"
                    value = "COVEREDRATIO"
                    minimum = "0.80".toBigDecimal()
                }
            }
        }
    }

    tasks.named("check") {
        dependsOn("jacocoTestCoverageVerification")
    }
}

project(":_infrastructure").afterEvaluate {

    val infraExcludes = listOf(
        "br/com/servicetrack/infrastructure/api/**",

        "br/com/servicetrack/infrastructure/ordemServico/**",

        "**/*Entity\$Companion*",

        "br/com/servicetrack/infrastructure/RestApplication*",
        "br/com/servicetrack/infrastructure/RestResourceRoot*"
    )

    tasks.named<JacocoReport>("jacocoTestReport") {
        classDirectories.setFrom(
            fileTree(layout.buildDirectory.dir("classes/kotlin/main")) {
                exclude(infraExcludes)
            },
            fileTree(layout.buildDirectory.dir("classes/java/main")) {
                exclude(infraExcludes)
            }
        )
        executionData.setFrom(
            fileTree(layout.buildDirectory.dir("jacoco")) {
                include("*.exec")
            }
        )

        doLast {
            printCoverageReport("Infrastructure", 0.60, reports.xml.outputLocation.get().asFile, branchRatio = 0.0)
        }
    }

    tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
        dependsOn(tasks.named("test"), tasks.named("jacocoTestReport"))

        classDirectories.setFrom(
            fileTree(layout.buildDirectory.dir("classes/kotlin/main")) {
                exclude(infraExcludes)
            },
            fileTree(layout.buildDirectory.dir("classes/java/main")) {
                exclude(infraExcludes)
            }
        )
        executionData.setFrom(
            fileTree(layout.buildDirectory.dir("jacoco")) {
                include("*.exec")
            }
        )

        violationRules {
            rule {
                limit {
                    counter = "INSTRUCTION"
                    value = "COVEREDRATIO"
                    minimum = "0.60".toBigDecimal()
                }
            }
        }
    }

    tasks.named("check") {
        dependsOn("jacocoTestCoverageVerification")
    }
}
