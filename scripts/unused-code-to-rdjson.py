import argparse
import json
from pathlib import Path
import xml.etree.ElementTree as ElementTree


def repository_path(value):
    normalized = value.replace("\\", "/")
    path = Path(normalized)
    if path.is_absolute():
        try:
            return path.resolve().relative_to(Path.cwd().resolve()).as_posix()
        except ValueError:
            return normalized
    return normalized.removeprefix("./")


def diagnostic(path, line, column, message, rule):
    return {
        "message": message,
        "location": {
            "path": repository_path(path),
            "range": {
                "start": {
                    "line": max(1, line),
                    "column": max(1, column),
                }
            },
        },
        "severity": "ERROR",
        "code": {"value": rule},
    }


def elements(node, name):
    return [child for child in node if child.tag.rsplit("}", 1)[-1] == name]


def read_checkstyle(path):
    findings = []
    root = ElementTree.parse(path).getroot()
    for file_node in elements(root, "file"):
        file_name = file_node.get("name", "")
        for error in elements(file_node, "error"):
            findings.append(diagnostic(
                file_name,
                int(error.get("line", "1")),
                int(error.get("column", "1")),
                error.get("message", "Unused import"),
                "UnusedImports",
            ))
    return findings


def read_pmd(path):
    findings = []
    root = ElementTree.parse(path).getroot()
    for file_node in elements(root, "file"):
        file_name = file_node.get("name", "")
        for violation in elements(file_node, "violation"):
            findings.append(diagnostic(
                file_name,
                int(violation.get("beginline", "1")),
                int(violation.get("begincolumn", "1")),
                " ".join((violation.text or "").split()),
                violation.get("rule", "UnusedVariable"),
            ))
    return findings


def unique_findings(findings):
    unique = {}
    for finding in findings:
        location = finding["location"]
        key = (
            location["path"],
            location["range"]["start"]["line"],
            location["range"]["start"]["column"],
            finding["message"],
        )
        unique[key] = finding
    return sorted(
        unique.values(),
        key=lambda finding: (
            finding["location"]["path"],
            finding["location"]["range"]["start"]["line"],
            finding["location"]["range"]["start"]["column"],
            finding["code"]["value"],
        ),
    )


def parse_arguments():
    parser = argparse.ArgumentParser()
    parser.add_argument("--checkstyle", type=Path, required=True)
    parser.add_argument("--pmd", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    return parser.parse_args()


def main():
    arguments = parse_arguments()
    findings = unique_findings(
        read_checkstyle(arguments.checkstyle) + read_pmd(arguments.pmd)
    )
    arguments.output.parent.mkdir(parents=True, exist_ok=True)
    content = "\n".join(json.dumps(finding, separators=(",", ":")) for finding in findings)
    arguments.output.write_text(content + ("\n" if content else ""), encoding="utf-8")


if __name__ == "__main__":
    main()
