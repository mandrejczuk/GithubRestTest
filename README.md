## How to Run

1. Clone this repository to your local machine.
2. Open a terminal and navigate to the project directory.
3. Build the project using Maven in your IDE

#### Request

- URL: `http://localhost:8080/api`
- Method: `GET`
- Headers:
  - `Accept: application/json`
  - `Accept: application/xml`

#### Request Example
  - URL: `http://localhost:8080/api?username=mandrejczuk`
  - Method: `GET`
  - Headers:
  - `Accept: application/json`
##### Parameters

- `username`: GitHub username (for JSON response).

#### Response

- Status Code: 200 (OK) or 404 (Not Found) for JSON response.
- Status Code: 406 (Not Acceptable) for XML response.

##### JSON Response Example

```json
[
  {
    "name": "Repository Name",
    "ownerLogin": "Owner Login",
    "branchesInfo": [
      {
        "name": "Branch Name",
        "last_commit_sha": "Last Commit SHA"
      }
    ]
  }
]
```
