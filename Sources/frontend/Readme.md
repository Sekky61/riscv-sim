# Next.js Frontend

Readme focused on development and build instructions of the web app.

## Structure

```
./src
  +--__tests__  - Tests
  +--app        - Pages of the application.
  +--components - Composable units of UI.
  +--constant   - Data
  +--lib        - Non-UI modules (Global state, API, types, etc.)
    +--__tests__- Tests
  +--styles     - Global styles
Readme.md       - Read me file
(Many configuration files)
```

Note: `src/components/base/ui` contains vendored code (shadcn/ui) that has only been modified, but not fully authored.

## Install steps

The app is run with bun, specifically `1.1.31`.
Package dependencies are defined in `package.json`.
They can be installed the usual way, by running this command in the frontend root (`Sources/frontend`):

```bash
bun install
```

## Parameters

The following environment variables can be set in `.env` file or in the command line.
Look in the `.env.example` for a specific example.
The variables must be specified at *build-time*.

### `NEXT_PUBLIC_BASE_PATH`

To deploy the application under a sub-path of a domain you can use this config option.

**Default:** `''`

**Examples:**
- `'/riscvsim'` if you want to deploy at `example.com/riscvsim`

### `NEXT_PUBLIC_EXTERNAL_SIM_API_PREFIX`

**Client** will use this string as the first part of URL to which simulation HTTP requests are sent.
It is important to include `http(s)://` for `localhost`.

**Default:** `http://localhost:8000` - this corresponds to the defaults of the simulation server

**Examples:**
- `http://localhost:8000` if the simulation server is running at `http://localhost:8000`
- `/api/sim` would send requests to the same domain and prefix the path with `/api/sim`. For example, if the app is on `example.com`, then a request could go to `example.com/api/sim/simulate`
- `''` which would send the request to the domain where the app is deployed, without any prefix

### `NEXT_PUBLIC_INTERNAL_SIM_API_PREFIX`

This is the prefix of URL used for simulation API requests originating from the server.

**Default:** `http://localhost:8000`

**Examples:**
- `simserver:8000` is used on the Docker network

### Setting the parameters in Docker

`Dockerfile` and `docker-compose` use `args` instead of environment variables.
The names are the same, without the `NEXT_PUBLIC_` prefix.
See the Dockerfile for details.

## Development

### Run the development server

You can start the server using this command:

```bash
bun run dev
```

Open [http://localhost:3000](http://localhost:3000) with your browser to see the app.
Other commands, like lint and typecheck are defined in `package.json`.

Notes:
- Changing the environment variables requires a server restart.

### Run tests

```bash
bun test
```

### Inspect bundle size

Inspired by [this article](https://blog.logrocket.com/how-analyze-next-js-app-bundles).

`ANALYZE=true bun run build` creates `client.html`, `nodejs.html`, and `edge.html`.

### Technology stack

- [Next.js](https://nextjs.org/)
- [React](https://reactjs.org/)
- [TypeScript](https://www.typescriptlang.org/)
- [Tailwind CSS](https://tailwindcss.com/)
- [biomejs](https://biomejs.dev/)

### The most important libraries used

- [Codemirror](https://codemirror.net/) - Code editor
- [Zod](https://www.npmjs.com/package/zod) - Data validation
- [Redux](https://redux.js.org/) - Global state management
- [lucide-react](https://lucide.dev/guide/packages/lucide-react) - Icons
- [react-hotkeys-hook](https://www.npmjs.com/package/react-hotkeys-hook) - Keyboard shortcuts
- [shadcn/ui](https://ui.shadcn.com/) - UI components

