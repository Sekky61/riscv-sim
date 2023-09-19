import { CompilerOptions } from './redux/compilerSlice';

export type APIResponse =
  | {
      '@type': string;
      success: true;
      program: string[];
      cLines: number[];
      asmToC: number[];
    }
  | {
      '@type': string;
      success: false;
      compilerError?: string;
    };

export async function callCompilerImpl(code: string, options: CompilerOptions) {
  // fetch from :8000/compile
  // payload:
  // {
  //   "@type": "com.gradle.superscalarsim.server.compile.CompileRequest",
  //   "code": string
  //   "optimize": boolean
  // }

  const response = await fetch('http://localhost:8000/compile', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      '@type': 'com.gradle.superscalarsim.server.compile.CompileRequest',
      code,
      optimize: options.optimize,
    }),
  });
  const json: APIResponse = await response.json();
  return json;
}
