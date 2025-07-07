export interface ESCPOSPrinterPlugin {
  // Bluetooth
  listPairedDevices(): Promise<{ devices: { name: string; address: string }[] }>;
  connect(options: { deviceName: string }): Promise<{ connected: boolean }>;
  disconnect(): Promise<{ disconnected: boolean }>;
  isConnected(): Promise<{ connected: boolean }>;

  // Impression texte
  printText(options: { text: string }): Promise<{ success: boolean }>;

  // ESC/POS avancé
  setBold(options: { bold: boolean }): Promise<{ success: boolean }>;
  setAlignment(options: { align: 'left' | 'center' | 'right' }): Promise<{ success: boolean }>;
  setTextSize(options: { size: number }): Promise<{ success: boolean }>;
  cutPaper(): Promise<{ success: boolean }>;

  // Wifi
  connectWifi(options: { ip: string; port: number }): Promise<{ connected: boolean }>;
  disconnectWifi(): Promise<{ disconnected: boolean }>;
  isWifiConnected(): Promise<{ connected: boolean }>;

  // Permissions Bluetooth
  checkPermissions(): Promise<{ bluetooth: string }>;
  requestPermissions(): Promise<{ bluetooth: string }>;

  // USB
  listUsbDevices(): Promise<{ devices: { name: string; vendorId: number; productId: number }[] }>;
  connectUsb(options: { vendorId: number; productId: number }): Promise<{ connected: boolean }>;
  disconnectUsb(): Promise<{ disconnected: boolean }>;
  isUsbConnected(): Promise<{ connected: boolean }>;

}
