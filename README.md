# checkov-jetbrains-idea

![Build](https://github.com/bridgecrewio/checkov-jetbrains-idea/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)

## Template ToDo list
- [x] Create a new [IntelliJ Platform Plugin Template][template] project.
- [ ] Verify the [pluginGroup](/gradle.properties), [plugin ID](/src/main/resources/META-INF/plugin.xml) and [sources package](/src/main/kotlin).
- [ ] Review the [Legal Agreements](https://plugins.jetbrains.com/docs/marketplace/legal-agreements.html).
- [ ] [Publish a plugin manually](https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html?from=IJPluginTemplate) for the first time.
- [ ] Set the Plugin ID in the above README badges.
- [ ] Set the [Deployment Token](https://plugins.jetbrains.com/docs/marketplace/plugin-upload.html).
- [ ] Click the <kbd>Watch</kbd> button on the top of the [IntelliJ Platform Plugin Template][template] to be notified about releases containing new features and fixes.

<!-- Plugin description -->

Checkov is a static code analysis tool for infrastructure-as-code.
The Checkov plugin for Jetbrains enables developers to get real-time scan results, as well as inline fix suggestions as they develop cloud infrastructure.


###Configuration
Sign up to a Bridgecrew Community account  [here](https://https://www.bridgecrew.cloud/).. If you already have an account, sign in and go to the next step.

From Integrations, select API Token and copy the API key.

In Jetbrains, enter your API Token in the Checkov settings page under tools.

Using a custom CA certificate is possible. If needed, set the path to the certificate file in the Checkov settings page.

###Usage
Checkov will run automatically on a file when:
1. A closed file is opened in your IDE.
2. A file is saved

Scan results will appear in checkov tool window in the bottom of your IDE.

Scan results will appear as a tree of results and be sorted by File Name -> Resource -> Policy Name including  violating policy and a link to step-by-step fix guidelines.
Click a scan to see its details. Details will include the violating policy and a link to step-by-step fix guidelines.
In most cases, the Details will include a fix option. This will either add, remove or replace an unwanted configuration, based on the Checkov fix dictionaries.

You can skip checks by adding an inline skip annotation checkov:skip=<check_id>:<suppression_comment>. For more details see the docs.

The extension will continue to scan file modifications and highlight errors in your editor upon every material resource modification.

###Troubleshooting logs
To access logs directory, go to Help -> Show log in IDE.


<!-- Plugin description end -->

## Installation

- Using IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "checkov-jetbrains-idea"</kbd> >
  <kbd>Install Plugin</kbd>
  
- Manually:

  Download the [latest release](https://github.com/bridgecrewio/checkov-jetbrains-idea/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>


---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
