# Contributing to Al-Huda

Thank you for your interest in contributing to Al-Huda! We welcome contributions from everyone, including bug fixes, translations, documentation improvements, and feature proposals.

---

## How to Contribute

### 1. Reporting Bugs
- Check the [Issues](https://github.com/saferill/Al-Huda/issues) tab first to see if the bug has already been reported.
- If not, open a new issue using the **Bug Report** template.
- Include your Android version, device model, app version, and steps to reproduce.

### 2. Suggesting Enhancements
- Open an issue using the **Feature Request** template.
- Clearly describe the problem you want solved and your proposed solution.

### 3. Submitting Code Changes (Pull Requests)
1. Fork the repository and create your branch from `main`:
   ```bash
   git checkout -b feature/your-feature-name
   ```
2. Follow existing code style (Kotlin, Jetpack Compose, Material 3 conventions).
3. Test your changes thoroughly on a device or emulator before submitting.
4. Ensure the project builds without errors:
   ```bash
   ./gradlew assembleDebug
   ```
5. Open a Pull Request against the `main` branch with a clear description of your changes.

### 4. Improving Translations
Translations live in `app/src/main/res/values-*/strings.xml`.
- When adding or editing strings, ensure special characters (such as single quotes `'`) are properly escaped with a backslash (`\'`).
- Use the standard language code directory (e.g. `values-ar`, `values-in`, `values-tr`).

---

## Code of Conduct

Please maintain a respectful, constructive, and welcoming environment for all contributors and users.
