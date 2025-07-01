export interface ESCPOSPrinterPlugin {
  print(options: { text: string }): Promise<{ success: boolean }>;
}
