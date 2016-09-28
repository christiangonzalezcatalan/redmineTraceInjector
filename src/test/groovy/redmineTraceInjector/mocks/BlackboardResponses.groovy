package redmineTraceInjector.Mocks

/**
 * Created by christian on 27-08-16.
 */
class BlackboardResponses {
    static String getMemberByEmailFromBlackboard(email, id, name) {
        """[
  {
    "id": ${id},
    "email": ${email},
    "name": ${name}
  }
]"""
    }

    static String getTraceFromBlackboard() {
        """[
  {
    "id": "57d5f5e48acec62fb22f8a73",
    "project": {
      "id": "57cc59368acec62bf2f7d7ed"
    },
    "taskTraces": [
      {
        "name": null,
        "status": "ESTADO!",
        "taskTraceId": "57d5fe9c8acec641a6c8d926",
        "traceDetails": [
          {
            "date": "2016-09-11T06:00:00Z",
            "hours": 5,
            "member": {
              "id": "57c3c4858acec662dab6dcf4"
            }
          }
        ]
      },
      {
        "name": null,
        "status": "ESTADO!",
        "taskTraceId": "57d5f5e28acec63dfc6b1317",
        "traceDetails": [
          {
            "date": "2016-08-30T06:00:00Z",
            "hours": 3,
            "member": {
              "id": "57c3c4858acec662dab6dcf4"
            }
          },
          {
            "date": "2016-08-29T06:00:00Z",
            "hours": 2,
            "member": {
              "id": "57c3c4858acec662dab6dcf4"
            }
          }
        ]
      }
    ]
  }
]"""
    }

    static String getTraceMappingsFromBlackboard() {
        """[
  {
    "id": "57d5f5e88acec62fb22f8a74",
    "map": [
      {"internalId": "57d5f5e28acec63dfc6b1317", "externalId": "9", "entityType":"Task"},
      {"internalId": "57d5fe9c8acec641a6c8d926", "externalId": "11", "entityType":"Task"}
    ],
    "project": {
      "id": "57cc59368acec62bf2f7d7ed"
    },
    "tool": "Redmine"
  }
]"""
    }

    static String getToolConfigurationFromBlackboard() {
        """[{
  "id": "57e9dc908acec651a7232ea6",
  "parameters": {
    "projectId": 2
  },
  "processNames": [
    "RedminePlanInjector",
    "RedmineTraceInjector"
  ],
  "project": {
    "id": "57ccad338acec633f77f862e"
  },
  "toolName": "Redmine"
}]"""
    }

    static String getProjectFromBlackboard() {
        """{
    "id": "57ccad338acec633f77f862e",
    "name": "Mi proyecto 2",
    "organization": {
      "id": "57e89b278acec6487695a4b5"
    }
}"""
    }

    static String getRepositoryFromBlackboard() {
        """[
  {
    "id": "57e8a96b8acec64b7e3c8422",
    "data": {
      "apiKey": "baa9da1d47247ea95bedc425027e7bb30df8f883",
      "root": "http://localhost:8081"
    },
    "organization": {
      "id": "57e89b278acec6487695a4b5"
    },
    "toolName": "Redmine"
  }
]"""
    }
}
