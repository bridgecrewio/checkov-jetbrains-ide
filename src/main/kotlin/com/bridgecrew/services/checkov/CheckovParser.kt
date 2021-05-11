import com.beust.klaxon.Json
import com.beust.klaxon.JsonReader
import com.beust.klaxon.Klaxon
import java.io.File
import java.io.StringReader

data class Summary(
    val passed: Int,
    val failed: Int,
    val skipped: Int,
    val parsing_errors: Int,
    val checkov_version: String
)

data class Result(
    val check_id: String,
    val check_name: String,
    @Json(serializeNull = false) val guideline: String?,
    @Json(serializeNull = false) val file_line_range: Array<Int>?
)

data class Results(
    val passed_checks: Array<Result>,
    val failed_checks: Array<Result>,
    val skipped_checks: Array<Result>,
    val parsing_errors: Array<Result>
)

data class Report(val check_type: String, val summary: Summary, val results: Results)

fun getFileContent(filePath: String): String {

    return File(filePath).readText()
}

fun jsonToData(jsonString: String): ArrayList<Report> {
    val klaxon = Klaxon()
    JsonReader(StringReader(jsonString)).use { reader ->
        val result = arrayListOf<Report>()
        reader.beginArray {
            while (reader.hasNext()) {
                val report = klaxon.parse<Report>(reader)
                if (report != null) {
                    result.add(report)
                }
            }
        }
        return result
    }
}

data class Diagnostic(
    val type: String,
    val id: String,
    val message: String,
    val fileLineRange: Array<Int>?,
    val guideline: String?,
    val source: String,
)

fun main() {
    val filePath = "tests/report.json"
    val file = getFileContent(filePath)
    val reports = jsonToData(file)
    val diagnostics = mutableListOf<Diagnostic>()

    for (report in reports) {
        val type = report.check_type
        var hasError = false
        if (report.summary.failed > 0) {
            hasError = true
        }

        if (hasError) {
            for (failedChecks in report.results.failed_checks) {
                val failure = Diagnostic(
                    type,
                    failedChecks.check_id,
                    failedChecks.check_name,
                    failedChecks.file_line_range,
                    failedChecks.guideline,
                    "Checkov"
                )

                diagnostics.add(failure)
            }
        }
    }

    println(diagnostics)
}