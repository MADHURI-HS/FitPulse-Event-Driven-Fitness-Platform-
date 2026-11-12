# FitPulse frontend

A minimal React client for the FitPulse backend: register / log in, log activities, and view the
AI-generated recommendation for each activity. It is a demo UI; the project's focus is the backend.

**Stack:** React 19, Vite, Redux Toolkit, React Router, Axios, Material UI.

## Run

The backend must be running first (`docker compose up --build` in the repository root); the client
talks to the API Gateway at `http://localhost:8080`.

```bash
npm install
npm run dev      # http://localhost:5173
npm run build    # production build (also run in CI)
```
