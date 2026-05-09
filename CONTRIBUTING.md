# Contributing Guide

Thank you for your interest in contributing to Hojo-Agentic-OS! We welcome contributions of all kinds.

For local Maven build order, `make build`, and dependency prerequisites, see **[docs/BUILD.md](docs/BUILD.md)**. For security vulnerabilities, please report privately per **[SECURITY.md](SECURITY.md)** — do not post in public Issues.

## Code of Conduct

By participating in this project, you agree to abide by **[CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md)**. Summary:

- Be respectful to all contributors
- Maintain friendly and constructive discussions
- Welcome new contributors and answer questions patiently
- Focus on what is best for the project

## How to Contribute

### Report a Bug

If you find a bug, please submit an Issue on GitHub:

1. Search existing Issues to confirm the problem hasn't been reported
2. Create a new Issue using the [Bug Report Template](https://github.com/HojoAI/Hojo-Agentic-OS/issues/new?template=bug_report.yml)
3. Provide clear reproduction steps and expected behavior
4. Include relevant logs, screenshots, or error messages

### Suggest a Feature

1. Search existing Issues to confirm the feature hasn't been proposed
2. Create a new Issue using the [Feature Request Template](https://github.com/HojoAI/Hojo-Agentic-OS/issues/new?template=feature_request.yml)
3. Clearly describe the use case and expected outcome
4. Wait for maintainer discussion and approval

### Submit Code

#### Development Environment Setup

```bash
# Clone the repository
git clone https://github.com/HojoAI/Hojo-Agentic-OS.git
cd Hojo-Agentic-OS

# Build and install modules to local ~/.m2 (no root pom.xml; order matches Makefile)
make build
```

#### Branch Strategy

- `main` - Main branch, kept stable
- `develop` - Development branch
- `feature/*` - Feature branches
- `bugfix/*` - Bug fix branches
- `hotfix/*` - Emergency fix branches

#### Submitting Pull Requests

1. Fork the repository to your GitHub account
2. Create a feature branch from `develop`: `git checkout -b feature/your-feature-name`
3. Make your changes and commit
4. Push to your Fork: `git push origin feature/your-feature-name`
5. Submit a Pull Request on GitHub

#### Code Standards

- Follow Google Java Style Guide
- Use IDE formatter tools (e.g., Google Java Format plugin for IntelliJ IDEA)
- Add necessary unit tests
- Ensure tests pass in the modules you touch: `mvn -f <module>/pom.xml test` (see docs/BUILD.md)
- Add Javadoc comments for public APIs

#### Commit Message Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

Type categories:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code formatting (no functional change)
- `refactor`: Refactoring
- `test`: Test-related
- `chore`: Build or auxiliary tools

Example:
```
feat(voice): add real-time translation support

Implement WebSocket-based real-time translation service
with automatic language detection and streaming support.

Closes #123
```

### Documentation Contributions

Documentation is equally important! You can contribute by:

- Fixing spelling errors and typos
- Improving clarity of existing documentation
- Adding code examples and tutorials
- Translating documentation to other languages

## Code Review Process

After submitting a PR:

1. Automated CI checks will run (build, tests, code quality checks)
2. Maintainers will conduct manual review
3. You may be asked to make changes
4. Once approved, your code will be merged

## Getting Help

- GitHub Issues: Technical questions and feature discussions
- GitHub Discussions: General questions and community exchange
- Security issues: See [SECURITY.md](SECURITY.md)

## License

By contributing code intended for inclusion in this repository, you agree that your contribution will be licensed under the **[same terms as the Work](LICENSE)**. That is the **Hojo AgenticOS License**: Apache License, Version 2.0 **with the additional conditions** stated in `LICENSE` (branding integrity and contributor clauses, including that the licensor may adjust this agreement and may use contributions commercially). It is **not** identical to the stock Apache License 2.0 text without those additions. If you require a different arrangement, coordinate with the maintainers **before** submitting substantial changes.

## Acknowledgments

Thank you to every contributor! Your contributions make this project better.
