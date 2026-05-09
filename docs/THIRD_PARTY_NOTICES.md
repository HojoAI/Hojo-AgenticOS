# Third-Party Software Notice

This document explains compliance points related to third-party software, cloud services, and licensing for **Hojo-Agentic-OS** in open-source distribution scenarios, and separately lists components that require **extra attention** compared to the repository's main license.

This document **does not constitute** legal advice; please evaluate based on your use case before redistribution or commercial use, and consult a legal advisor when necessary.

---

## 1. Repository License and Apache 2.0

- License conditions for this project are based on **[LICENSE](../LICENSE)** at the repository root (modified **Apache License, Version 2.0**, with additional conditions).
- Full text of **Apache License 2.0**: <https://www.apache.org/licenses/LICENSE-2.0>
- **Trademark** usage restrictions in **[TRADEMARK.md](TRADEMARK.md)**; third-party product names, trademarks, and Logos belong to their respective owners.

---

## 2. General Notes on Open-Source Dependencies

- This repository introduces many **transitive dependencies** (e.g., Spring ecosystem, common utility libraries, etc.) during build and runtime. Most of these use **Apache-2.0, MIT, BSD** and other open-source licenses **commonly compatible** with Apache-2.0.
- **This document does not enumerate** the above routine dependencies one by one to avoid inconsistency with the actual dependency resolution of build artifacts; **use the dependency tree and respective licenses resolved during your local or CI build as the standard**.
- **Recommended before releasing or redistributing binaries/container images**:
  - Use build tools to generate **complete dependency manifests** and **license reports** (e.g., Maven: `mvn license:download-licenses` / third-party `license-maven-plugin`; Gradle: `com.github.jk1.dependency-license-report`, etc.);
  - Conduct special verification on **GPL/AGPL/LGPL** and other **copyleft** licenses to confirm compatibility with your distribution method (especially SaaS, static linking, container image layers);
  - If Apache 2.0 **NOTICE** requirements need to be met, you may aggregate each dependency's attached `NOTICE` into this repository's `NOTICE` file (if applicable).

---

## 3. Obligations on Redistribution (Summary)

When you provide this software or derivative works (including executables, images, dependency packaged products) to third parties, you typically need to:

- Attach this repository's **[LICENSE](../LICENSE)** (and legally required third-party licenses and notices);
- **Retain** copyright, patent, trademark, and attribution notices already present in source code or documentation (see **Branding integrity** and other additional conditions in LICENSE);
- For components with **non-Apache-2.0** or **proprietary licenses** (see "High-Risk Items" below), **separately comply** with their licenses and service terms, **do not** default to treating them as the same as the main repository license.

---

## 4. Cloud Services and APIs

Some features depend on **third-party online services** (e.g., speech recognition, RTC, etc.). When using such services, in addition to software licenses, you must also comply with corresponding **cloud platform service terms**, **privacy and compliance** (data cross-border, log retention, industry regulations, etc.) requirements; **this repository's documentation does not replace** the official terms of each service provider.

---

## 5. High-Risk Items (Significantly Different from Main License or Requiring Separate Authorization)

The following components are **not equivalent** to the repository's main license (modified Apache-2.0); before integrating, redistributing, or delivering products containing these components to customers, **you must read and comply with** their official terms and obtain necessary authorizations.

### 5.1 Microsoft Cognitive Services Speech SDK (Java)

- **Maven coordinates**: `com.microsoft.cognitiveservices.speech:client-sdk`
- **License**: Microsoft Software License Terms (**NOT** Apache-2.0 or other open-source licenses)
- **Official terms**: <https://aka.ms/csspeech/license>
- **Maven metadata**: <https://repo1.maven.org/maven2/com/microsoft/cognitiveservices/speech/client-sdk/>
- **Capabilities used**: Voice gateway related capabilities (e.g., translation, speech recognition, etc., based on actual code)

**Note**: When using this SDK or distributing binary products containing this SDK, you must comply with Microsoft's license terms; when using Azure Speech and other cloud services, you must also comply with **Azure service terms** and compliance requirements.

### 5.2 DingTalk RTC Linux Java SDK

- **Maven coordinates**: `com.ding.rtc.linux:ding-rtc-linux-java` (version used in the project such as `3.9.0`)
- **License**: Generally DingTalk/Open Platform's **proprietary license or service agreement**, **NOT** Apache-2.0. When Maven Central public repository **does not** have a release of this coordinate, actual JARs mostly come from enterprise private servers or DingTalk official distribution channels, **subject to the terms attached when you obtained the SDK**.
- **Capabilities used**: Sysagent, RTC related capabilities (based on actual code)

**Note**: Before integrating, redistributing, or delivering products containing this SDK to third parties, you must obtain authorization from DingTalk officially and comply with their license and redistribution requirements.

---

## 6. Maintenance and Updates

- If adding or upgrading dependencies leads to license type changes (especially introducing **copyleft, proprietary, commercial** licenses), maintainers should update dependency manifests or corresponding sections of this document.
- If you find other components requiring separate notices, feel free to supplement via **Issue / PR**, and try to attach **official license links** and **Maven/Gradle coordinates** (or acquisition channel description).

---

## 7. Disclaimer

This document is written based on knowledge at the time of maintenance; dependency versions and licenses may change over time. **No guarantee** is made for the completeness, accuracy, or timeliness of third-party component license terms. Risks of use and redistribution are borne by the user.
