package icons

import com.intellij.openapi.util.IconLoader

object CheckovIcons {

    val ErrorIcon = IconLoader.getIcon("/icons/balloonError.svg", javaClass)
    val TerraformIcon = IconLoader.getIcon("/icons/terraform.svg", javaClass)
    val SeverityUnknown = IconLoader.getIcon("/icons/severity_unknown.svg", javaClass)
    val SeverityLow = IconLoader.getIcon("/icons/severity_low.svg", javaClass)
    val SeverityMedium = IconLoader.getIcon("/icons/severity_medium.svg", javaClass)
    val SeverityHigh = IconLoader.getIcon("/icons/severity_high.svg", javaClass)
    val SeverityCritical = IconLoader.getIcon("/icons/severity_critical.svg", javaClass)
}