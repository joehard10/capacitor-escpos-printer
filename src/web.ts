import { WebPlugin } from '@capacitor/core';

import type { ESCPOSPrinterPlugin } from './definitions';

export class ESCPOSPrinterWeb extends WebPlugin implements ESCPOSPrinterPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
