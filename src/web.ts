import { WebPlugin } from '@capacitor/core';
import type { ESCPOSPrinterPlugin } from './definitions';

export class ESCPOSPrinterWeb extends WebPlugin implements ESCPOSPrinterPlugin {
  // Bluetooth
  async listPairedDevices(): Promise<{ devices: { name: string; address: string }[] }> {
    console.warn('Web: listPairedDevices not available');
    return { devices: [] };
  }

  async connect(): Promise<{ connected: boolean }> {
    console.warn('Web: connect not available');
    return { connected: false };
  }

  async disconnect(): Promise<{ disconnected: boolean }> {
    console.warn('Web: disconnect not available');
    return { disconnected: true };
  }

  async isConnected(): Promise<{ connected: boolean }> {
    console.warn('Web: isConnected not available');
    return { connected: false };
  }

  // Impression texte
  async printText(): Promise<{ success: boolean }> {
    console.warn('Web: printText not available');
    return { success: false };
  }

  async setBold(): Promise<{ success: boolean }> {
    console.warn('Web: setBold not available');
    return { success: false };
  }

  async setAlignment(): Promise<{ success: boolean }> {
    console.warn('Web: setAlignment not available');
    return { success: false };
  }

  async setTextSize(): Promise<{ success: boolean }> {
    console.warn('Web: setTextSize not available');
    return { success: false };
  }

  async cutPaper(): Promise<{ success: boolean }> {
    console.warn('Web: cutPaper not available');
    return { success: false };
  }

  // Wifi
  async connectWifi(): Promise<{ connected: boolean }> {
    console.warn('Web: connectWifi not available');
    return { connected: false };
  }

  async disconnectWifi(): Promise<{ disconnected: boolean }> {
    console.warn('Web: disconnectWifi not available');
    return { disconnected: true };
  }

  async isWifiConnected(): Promise<{ connected: boolean }> {
    console.warn('Web: isWifiConnected not available');
    return { connected: false };
  }

  // Permissions Bluetooth
  async checkPermissions(): Promise<{ bluetooth: string }> {
    console.warn('Web: checkPermissions not available');
    return { bluetooth: 'denied' };
  }

  async requestPermissions(): Promise<{ bluetooth: string }> {
    console.warn('Web: requestPermissions not available');
    return { bluetooth: 'denied' };
  }

  // USB
  async listUsbDevices(): Promise<{ devices: { name: string; vendorId: number; productId: number }[] }> {
    console.warn('Web: listUsbDevices not available');
    return { devices: [] };
  }

  async connectUsb(): Promise<{ connected: boolean }> {
    console.warn('Web: connectUsb not available');
    return { connected: false };
  }

  async disconnectUsb(): Promise<{ disconnected: boolean }> {
    console.warn('Web: disconnectUsb not available');
    return { disconnected: true };
  }

  async isUsbConnected(): Promise<{ connected: boolean }> {
    console.warn('Web: isUsbConnected not available');
    return { connected: false };
  }
}
