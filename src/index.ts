import { registerPlugin } from '@capacitor/core';

import type { ESCPOSPrinterPlugin } from './definitions';

const ESCPOSPrinter = registerPlugin<ESCPOSPrinterPlugin>('ESCPOSPrinter', {
  web: () => import('./web').then((m) => new m.ESCPOSPrinterWeb()),
});

export * from './definitions';
export { ESCPOSPrinter };
