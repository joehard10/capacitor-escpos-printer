export interface ESCPOSPrinterPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
