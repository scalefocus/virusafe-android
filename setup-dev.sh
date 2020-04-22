#!/bin/bash
set -euo pipefail

# Setup sample app credentials file.
cp gradle-example.properties gradle.properties
# Setup sample Google Services credentials file.
cp google-services-example.json presentation/google-services.json
# Setup locale files.
cp -a locale-example locale
# Setup translations build step.
cp service/translations-example.gradle service/translations.gradle