[![checkov](https://raw.githubusercontent.com/bridgecrewio/checkov/master/docs/web/images/checkov_by_bridgecrew.png)](https://checkov.io)

[![Maintained by Bridgecrew.io](https://img.shields.io/badge/maintained%20by-bridgecrew.io-blueviolet)](https://bridgecrew.io/?utm_source=github&utm_medium=organic_oss&utm_campaign=checkov-vscode)
![Build](https://github.com/bridgecrewio/checkov-jetbrains-idea/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/17721-checkov.svg)](https://plugins.jetbrains.com/plugin/17721-checkov)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/17721-checkov.svg)](https://plugins.jetbrains.com/plugin/17721-checkov)
[![slack-community](https://img.shields.io/badge/Slack-contact%20us-lightgrey.svg?logo=slack)](https://slack.bridgecrew.io/?utm_source=github&utm_medium=organic_oss&utm_campaign=checkov-intellij)

# Checkov Plugin for Jetbrains IDEA

[Checkov](https://github.com/bridgecrewio/checkov) is a static code analysis tool for infrastructure-as-code.

The Checkov Plugin for Intellij enables developers to get real-time scan results, as well as inline fix suggestions as they develop cloud infrastructure.


<!-- TODO PLUGIN GIF DEMO -->
<!-- Plugin description -->

The plugin is currently available for download directly from the [IntelliJ Plugin Marketplace](https://plugins.jetbrains.com/plugin/17721-checkov) and its source code is available in an [Apache 2.0 licensed repository](https://github.com/bridgecrewio/checkov-jetbrains-ide).

Activating the plugin requires submission of one-time Bridgecrew API Token that can be obtained by [creating a new Bridgecrew platform account](https://docs.bridgecrew.io/docs/get-api-token). It uses open [Bridgecrew Developer APIs](https://docs.bridgecrew.io/reference) to evaluate code and offer automated inline fixes. For more information about data shared with Bridgecrew see the [Disclaimer](#disclaimer) section below).

Plugin features include:

* [1000+ built-in policies](https://github.com/bridgecrewio/checkov/blob/master/docs/5.Policy%20Index/all.md) covering security and compliance best practices for AWS, Azure and Google Cloud.
* Terraform, Terraform Plan, CloudFormation, Kubernetes, Helm, Serverless and ARM template scanning.
* Detects [AWS credentials](https://github.com/bridgecrewio/checkov/blob/master/docs/2.Basics/Scanning%20Credentials%20and%20Secrets.md) in EC2 Userdata, Lambda environment variables and Terraform providers.
* In Terraform and CloudFormation checks support evaluation of arguments expressed in [variables](https://github.com/bridgecrewio/checkov/blob/master/docs/2.Basics/Handling%20Variables.md) and remote modules to their actual values.
* Supports inline [suppression](https://github.com/bridgecrewio/checkov/blob/master/docs/2.Basics/Suppressing%20and%20Skipping%20Policies.md) via comments.
* Links to policy descriptions, rationales as well as step by step instructions for fixing known misconfigurations.
* Fix suggestions for commonly misconfigured Terraform and CloudFormation attributes.


## Getting started

### Install

- Using IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "checkov"</kbd> >
  <kbd>Install Plugin</kbd>
  
- Manually:

  Download the [latest release](https://github.com/bridgecrewio/checkov-jetbrains-idea/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>


### Dependencies

* [Python](https://www.python.org/downloads/) >= 3.7 or [Pipenv](https://docs.pipenv.org/) or [Docker](https://www.docker.com/products/docker-desktop) daemon running

The Checkov plugin will invoke the latest version of ```Checkov```.

### Configuration

* Sign up to a Bridgecrew Community account [here](https://bridgecrew.cloud/). If you already have an account, sign in and go to the next step.

* From [Integrations](https://www.bridgecrew.cloud/integrations/api-token), select **API Token** and copy the API key.
* In Jetbrains, enter your API Token in the Checkov plugin settings page under tools.  
* Using a custom CA certificate is possible. If needed, set the path to the certificate file in the Checkov plugin settings page.

### Usage

* Open a file you wish to scan with checkov in IntelliJ.
* Checkov will run automatically everytime an IaC is opened or saved.
* Scan results should now appear in the checkov tool window in the bottom of your IDE.
* Scan results will appear on the left side as a tree of File Names -> Resources -> Violated checks.
* Click a check to see its details. Details including  violating policy and a link to step-by-step fix guidelines.
* In most cases, the Details will include a fix option. This will either add, remove or replace an unwanted configuration, based on the Checkov fix dictionaries.
* You can skip checks by adding an inline skip annotation ```checkov:skip=<check_id>:<suppression_comment>```. For more details see the [docs](https://github.com/bridgecrewio/checkov/blob/master/docs/2.Concepts/Suppressions.md).
* To get Checkov results updated as you code you can configure the IDE to autosave modified files at regular time intervals.

### Troubleshooting logs

To access checkov-intellij logs directory, go to `Help` and select `Show Log in Finder` (for macOS) or `Show Log in Explorer` (for Windows).

## Contributing

Contribution is welcomed!

Start by reviewing the [contribution guidelines](https://github.com/bridgecrewio/checkov/blob/master/CONTRIBUTING.md). After that, take a look at a [good first issue](https://github.com/bridgecrewio/checkov/issues?q=is%3Aissue+is%3Aopen+label%3A"good+first+issue").

Looking to contribute new checks? Learn how to write a new check (AKA policy) [here](https://github.com/bridgecrewio/checkov/blob/master/docs/5.Contribution/New-Check.md).

## Disclaimer

To use this checkov-jetbrains plugin, you will need to create a free account at bridgecrew.cloud using your e-mail, the plugin uses Bridgecrew.cloud's fixes API to analyse and produce code fixes, and enrich the results provided into jetbrains IDE. Please notice bridgecrew [privacy policy](https://bridgecrew.io/privacy-policy/?utm_source=github&utm_medium=organic_oss&utm_campaign=checkov-vscode) for more details on collected data when using bridgecrew application.
To generate fixes, files found to have triggered checkov violations are made available to the fixes API for the sole purpose of generating inline fixes code recommendations.

## Support

[Bridgecrew](https://bridgecrew.io/?utm_source=github&utm_medium=organic_oss&utm_campaign=checkov-vscode) builds and maintains Checkov to make policy-as-code simple and accessible.

Start with our [Documentation](https://bridgecrewio.github.io/checkov/) for quick tutorials and examples.

If you need direct support you can contact us at [info@bridgecrew.io](mailto:info@bridgecrew.io).

---
The plugin is based on the [Jetbrains Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template

<!-- Plugin description end -->
