'use server';

import {
  basePath,
  isLocal,
  isProd,
  showLogger,
  simApiExternalPrefix,
  simApiInternalPrefix,
} from './env';

function envObject() {
  return {
    simApiExternalPrefix,
    simApiInternalPrefix,
    isLocal,
    isProd,
    showLogger,
    basePath,
  };
}

export type EnvContextType = ReturnType<typeof envObject>;

/**
 * Server side function to pull env to client at runtime
 */
export const env = async () => {
  return envObject();
};
