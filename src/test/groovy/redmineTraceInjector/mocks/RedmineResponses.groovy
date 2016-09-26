package redmineTraceInjector.Mocks

/**
 * Created by christian on 27-08-16.
 */
class RedmineResponses {
    static String getUserFromRedmine(id) {
        """{
  "user": {
    "id": ${id},
    "login": "cgonzalez",
    "firstname": "Christian",
    "lastname": "González",
    "mail": "${id==3?'christiangonzalezcatalan@hotmail.com':'jperez@miempresita.cl'}",
    "created_on": "2015-08-03T03:11:06Z",
    "last_login_on": "2016-08-19T03:46:40Z",
    "api_key": "baa9da1d47247ea95bedc425027e7bb30df8f883",
    "status": 1
  }
}"""
    }

    static String listarRegistrosHorasTrabajadas()
    {
      """{
  "time_entries": [
    {
      "id": 10,
      "project": {
        "id": 3,
        "name": "Dashboard Gems"
      },
      "issue": {
        "id": 9
      },
      "user": {
        "id": 3,
        "name": "Christian González"
      },
      "activity": {
        "id": 9,
        "name": "Development"
      },
      "hours": 3,
      "comments": "",
      "spent_on": "2016-08-30",
      "created_on": "2016-08-30T04:00:20Z",
      "updated_on": "2016-08-30T04:00:20Z"
    },
    {
      "id": 11,
      "project": {
        "id": 3,
        "name": "Dashboard Gems"
      },
      "issue": {
        "id": 9
      },
      "user": {
        "id": 3,
        "name": "Christian González"
      },
      "activity": {
        "id": 9,
        "name": "Development"
      },
      "hours": 2,
      "comments": "",
      "spent_on": "2016-08-29",
      "created_on": "2016-08-30T04:03:17Z",
      "updated_on": "2016-08-30T04:03:17Z"
    }
  ],
  "total_count": 2,
  "offset": 0,
  "limit": 50
}"""
    }

    static String getTimeEntriesFromRedmine() {
        """{
  "time_entries": [
    {
      "id": 12,
      "project": {
        "id": 3,
        "name": "Dashboard Gems"
      },
      "issue": {
        "id": 11
      },
      "user": {
        "id": 3,
        "name": "Christian González"
      },
      "activity": {
        "id": 9,
        "name": "Development"
      },
      "hours": 5,
      "comments": "",
      "spent_on": "2016-09-11",
      "created_on": "2016-09-12T00:54:22Z",
      "updated_on": "2016-09-12T00:54:22Z"
    },
    {
      "id": 10,
      "project": {
        "id": 3,
        "name": "Dashboard Gems"
      },
      "issue": {
        "id": 9
      },
      "user": {
        "id": 3,
        "name": "Christian González"
      },
      "activity": {
        "id": 9,
        "name": "Development"
      },
      "hours": 3,
      "comments": "",
      "spent_on": "2016-08-30",
      "created_on": "2016-08-30T04:00:20Z",
      "updated_on": "2016-08-30T04:00:20Z"
    },
    {
      "id": 11,
      "project": {
        "id": 3,
        "name": "Dashboard Gems"
      },
      "issue": {
        "id": 9
      },
      "user": {
        "id": 3,
        "name": "Christian González"
      },
      "activity": {
        "id": 9,
        "name": "Development"
      },
      "hours": 2,
      "comments": "",
      "spent_on": "2016-08-29",
      "created_on": "2016-08-30T04:03:17Z",
      "updated_on": "2016-08-30T04:03:17Z"
    }
  ],
  "total_count": 3,
  "offset": 0,
  "limit": 25
}"""
    }

    static String getIssueFromRedmine(id, subject) {
        """{
  "issue": {
    "id": ${id},
    "project": {
      "id": 3,
      "name": "Dashboard Gems"
    },
    "tracker": {
      "id": 1,
      "name": "Bug"
    },
    "status": {
      "id": 2,
      "name": "In Progress"
    },
    "priority": {
      "id": 2,
      "name": "Normal"
    },
    "author": {
      "id": 3,
      "name": "Christian González"
    },
    "assigned_to": {
      "id": 4,
      "name": "Juan Pérez"
    },
    "subject": "${subject}",
    "description": "Revisión de código de acuerdo a buenas prácticas definidas.",
    "start_date": "2016-08-28",
    "due_date": "2016-09-13",
    "done_ratio": 0,
    "spent_hours": 5,
    "created_on": "2016-08-29T02:24:18Z",
    "updated_on": "2016-08-29T02:24:18Z"
  }
}"""
    }
}
