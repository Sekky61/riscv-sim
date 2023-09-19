# Next.js frontend

Technology stack:

- [Next.js](https://nextjs.org/)
- [React](https://reactjs.org/)
- [TypeScript](https://www.typescriptlang.org/)
- [Tailwind CSS](https://tailwindcss.com/)
- [Jest](https://jestjs.io/)
- [ESLint](https://eslint.org/)

## Structure

```
./src
  +--app        - Pages of the application.
  +--components - Units of UI.
  +--constant   - Data (examples, descriptions of instructions)
  +--lib        - Non-UI modules (Global state, API, etc.)
  +--styles     - Global styles
Readme.md       - Read me file
```

### Install steps

To install the dependencies, run this command:

```bash
npm install
```

### Run the development server

You can start the server using this command:

```bash
npm run dev
```

Open [http://localhost:3000](http://localhost:3000) with your browser to see the result.
Other commands, like lint are defined in `package.json`.

### Inspect bundle size

Inspired by [this article](https://blog.logrocket.com/how-analyze-next-js-app-bundles).

`ANALYZE=true npm run build` creates `client.html`, `nodejs.html`, and `edge.html`.

### Main used libraries

- [Codemirror](https://codemirror.net/) - Code editor
- [Zod](https://www.npmjs.com/package/zod) - Data validation
- [Redux](https://redux.js.org/) - Global state management
- [lucide-react](https://lucide.dev/guide/packages/lucide-react) - Icons
- [react-hotkeys-hook](https://www.npmjs.com/package/react-hotkeys-hook) - Keyboard shortcuts
- [react-modal](https://www.npmjs.com/package/react-modal) - Modal
- [reapop](https://www.npmjs.com/package/reapop) - Notifications
