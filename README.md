# Commit Analyzer Microservice

This is a Spring Boot Java microservice that fetches GitHub commit changes for a selected date range and returns file-by-file change details.

## Features
- Fetches commits from another GitHub repository
- Supports a selected date range
- Retrieves file changes for each commit
- Aggregates per-file additions, deletions, and patch diffs
- Returns a JSON response for downstream UI or reporting tools

## Tech stack
- Java 17
- Spring Boot 3.3.x
- Maven
- GitHub REST API

## Run locally

1. Build the project:
   ```bash
   mvn clean package
   ```

2. Start the microservice:
   ```bash
   java -jar target/commit-analyzer-0.0.1-SNAPSHOT.jar
   ```

3. Call the API:
   ```bash
   curl "http://localhost:8080/api/v1/commits/analyze?owner=microsoft&repo=vscode&from=2024-01-01&to=2024-01-31"
   ```

Optional GitHub token for higher rate limits:
```bash
export GITHUB_TOKEN=your_pat_here
java -jar target/commit-analyzer-0.0.1-SNAPSHOT.jar
```

## Response format

```json
{
  "owner": "microsoft",
  "repo": "vscode",
  "from": "2024-01-01",
  "to": "2024-01-31",
  "totalCommits": 14,
  "totalFilesChanged": 23,
  "commits": [
    {
      "sha": "abc123",
      "message": "Fix login bug",
      "committedAt": "2024-01-12T10:00:00Z",
      "files": [
        "src/auth/login.ts"
      ]
    }
  ],
  "fileChanges": [
    {
      "path": "src/auth/login.ts",
      "status": "modified",
      "additions": 25,
      "deletions": 6,
      "totalChangedLines": 31,
      "patchFragments": [
        "@@ -1,2 +1,3 @@\n..."
      ],
      "commitShas": [
        "abc123"
      ],
      "commitMessages": [
        "Fix login bug"
      ]
    }
  ]
}
```

## Notes
- GitHub's public API has rate limits for unauthenticated requests.
- Using a PAT increases the limit and makes the service more reliable.
- The service does not store commit data; it fetches and summarizes data on demand.
