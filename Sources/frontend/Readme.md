# Next.js Frontend

Readme focused on development and build instructions.

Technology stack:

- [Next.js](https://nextjs.org/)
- [React](https://reactjs.org/)
- [TypeScript](https://www.typescriptlang.org/)
- [Tailwind CSS](https://tailwindcss.com/)
- [Vitest]()
- [biomejs](https://biomejs.dev/)

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
Many configuration files
```

Note: `src/components/base/ui` contains vendored code (shadcn/ui) that has only been modified, but not fully authored.

### Install steps

The app was developed using npm `10.2.3` and node.js `v21.2.0`.
Package dependencies are defined in `package.json`.
They can be installed the usual way, by running this command in the frontend root (`Sources/frontend`):

```bash
npm install
```

### Define environment variables

The variables do have default values, but you can override them.
Dockerfile sets these variables.

- `NEXT_PUBLIC_SIMSERVER_PORT`
- `NEXT_PUBLIC_SIMSERVER_HOST`

Also, you can use the `.env` file. See `.env.example` for inspiration.

### Run the development server

You can start the server using this command:

```bash
npm run dev
```

Open [http://localhost:3000](http://localhost:3000) with your browser to see the app.
Other commands, like lint and typecheck are defined in `package.json`.

Notes:
- Changing the environment variables requires a server restart.

### Inspect bundle size

Inspired by [this article](https://blog.logrocket.com/how-analyze-next-js-app-bundles).

`ANALYZE=true npm run build` creates `client.html`, `nodejs.html`, and `edge.html`.

### The most important libraries used

- [Codemirror](https://codemirror.net/) - Code editor
- [Zod](https://www.npmjs.com/package/zod) - Data validation
- [Redux](https://redux.js.org/) - Global state management
- [lucide-react](https://lucide.dev/guide/packages/lucide-react) - Icons
- [react-hotkeys-hook](https://www.npmjs.com/package/react-hotkeys-hook) - Keyboard shortcuts
- [shadcn/ui](https://ui.shadcn.com/) - UI components
