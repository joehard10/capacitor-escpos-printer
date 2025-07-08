package cg.sijit.escpos;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.*;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresPermission;
import androidx.core.content.ContextCompat;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;

import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

/**
 * Implémentation ESC/POS pour imprimantes Bluetooth et Wifi
 */
public class ESCPOSPrinter {
    private static final String TAG = "ESCPOSPrinter";

    private Context context;

    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothSocket socket = null;
    private OutputStream outputStream = null;
    private boolean isConnected = false;

    private Socket wifiSocket = null;
    private OutputStream wifiOutputStream = null;
    private boolean isWifiConnected = false;



    private UsbManager usbManager;
    private UsbDeviceConnection usbConnection = null;
    private UsbEndpoint usbEndpoint = null;
    private boolean isUsbConnected = false;

    private static final String ACTION_USB_PERMISSION = "cg.sijit.escpos.USB_PERMISSION";




    public ESCPOSPrinter(Context context) {
        this.context = context;
        this.usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        ContextCompat.registerReceiver(this.context, this.usbPermissionReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    private boolean hasBluetoothConnectPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true; // Sur Android < 12 pas besoin
        }
    }

    /**
     * Lister les appareils Bluetooth déjà appairés
     */
    public JSArray listPairedDevices() {
        JSArray devicesArray = new JSArray();

        if (bluetoothAdapter == null) {
            Log.w(TAG, "Bluetooth non supporté sur cet appareil");
            return devicesArray;
        }

        if (!hasBluetoothConnectPermission()) {
            Log.w(TAG, "Pas de permission BLUETOOTH_CONNECT pour lister");
            return devicesArray;
        }

        try {
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            for (BluetoothDevice device : pairedDevices) {
                JSObject dev = new JSObject();
                dev.put("name", device.getName());
                dev.put("address", device.getAddress());
                devicesArray.put(dev);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Erreur de permission listPairedDevices", e);
        }
        return devicesArray;
    }

    /**
     * Se connecter à une imprimante Bluetooth par nom
     */
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    public boolean connect(String deviceName) throws Exception {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            throw new Exception("Bluetooth non disponible ou désactivé");
        }
        if (!hasBluetoothConnectPermission()) {
            throw new Exception("Permission BLUETOOTH_CONNECT manquante");
        }

        BluetoothDevice printer = findDeviceByName(deviceName);
        if (printer == null) {
            throw new Exception("Imprimante non trouvée : " + deviceName);
        }

        try {
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // SPP
            socket = printer.createRfcommSocketToServiceRecord(uuid);
            socket.connect();
            outputStream = socket.getOutputStream();
            isConnected = true;
            Log.i(TAG, "Connecté à " + deviceName);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Échec connexion Bluetooth", e);
            if (socket != null) try { socket.close(); } catch (Exception ignore) {}
            throw new Exception("Connexion Bluetooth échouée : " + e.getMessage());
        }
    }

    /**
     * Déconnecter proprement Bluetooth
     */
    public void disconnect() {
        try {
            if (outputStream != null) outputStream.close();
            if (socket != null) socket.close();
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de disconnect", e);
        }
        isConnected = false;
        Log.i(TAG, "Déconnecté Bluetooth");
    }

    /**
     * Vérifier si connecté
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Trouver une imprimante déjà appairée par nom
     */
    private BluetoothDevice findDeviceByName(String name) {
        if (bluetoothAdapter == null) return null;
        if (!hasBluetoothConnectPermission()) {
            Log.w(TAG, "Pas de permission BLUETOOTH_CONNECT pour findDeviceByName");
            return null;
        }

        try {
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName() != null && device.getName().equals(name)) {
                    return device;
                }
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Erreur permission findDeviceByName", e);
        }
        return null;
    }

    /**
     * Imprimer du texte
     */
    public void printText(String text) throws Exception {
        if (isConnected && outputStream != null) {
            outputStream.write(text.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            Log.i(TAG, "Texte imprimé via Bluetooth");
        } else if (isWifiConnected && wifiOutputStream != null) {
            wifiOutputStream.write(text.getBytes(StandardCharsets.UTF_8));
            wifiOutputStream.flush();
            Log.i(TAG, "Texte imprimé via Wifi");
        } else if (isUsbConnected && usbConnection != null && usbEndpoint != null) {
            byte[] data = text.getBytes(StandardCharsets.UTF_8);
            int result = usbConnection.bulkTransfer(usbEndpoint, data, data.length, 1000);
            if (result < 0) throw new Exception("USB bulk transfer failed");
            Log.i(TAG, "Texte imprimé via USB");
        }
        else {
            throw new Exception("Aucune imprimante connectée (Bluetooth ou Wifi)");
        }
    }

    /**
     * Changer le style (gras)
     */
    public void setBold(boolean bold) throws Exception {
        if (!isConnected || outputStream == null) throw new Exception("Non connecté");
        byte[] cmd = bold ? new byte[]{0x1B, 0x45, 0x01} : new byte[]{0x1B, 0x45, 0x00};
        outputStream.write(cmd);
        outputStream.flush();
        Log.i(TAG, "Bold " + (bold ? "activé" : "désactivé"));
    }

    /**
     * Changer l'alignement
     */
    public void setAlignment(String align) throws Exception {
        if (!isConnected || outputStream == null) throw new Exception("Non connecté");
        byte alignByte = 0;
        if ("center".equalsIgnoreCase(align)) alignByte = 1;
        else if ("right".equalsIgnoreCase(align)) alignByte = 2;
        byte[] cmd = new byte[]{0x1B, 0x61, alignByte};
        outputStream.write(cmd);
        outputStream.flush();
        Log.i(TAG, "Alignement mis à " + align);
    }

    /**
     * Changer taille texte
     */
    public void setTextSize(int size) throws Exception {
        if (!isConnected || outputStream == null) throw new Exception("Non connecté");
        byte sizeByte = (size == 2) ? (byte) 0x11 : (size == 3) ? (byte) 0x22 : 0x00;
        byte[] cmd = new byte[]{0x1D, 0x21, sizeByte};
        outputStream.write(cmd);
        outputStream.flush();
        Log.i(TAG, "Taille texte mise à " + size);
    }

    /**
     * Couper le papier
     */
    public void cutPaper() throws Exception {
        if (!isConnected || outputStream == null) throw new Exception("Non connecté");
        byte[] cmd = new byte[]{0x1D, 0x56, 0x00}; // full cut
        outputStream.write(cmd);
        outputStream.flush();
        Log.i(TAG, "Cut paper command envoyée");
    }

    /**
     * Connexion Wifi
     */
    public boolean connectWifi(String ip, int port) throws Exception {
        if (ip == null || ip.isEmpty()) throw new Exception("IP manquante");
        try {
            wifiSocket = new Socket(ip, port);
            wifiOutputStream = wifiSocket.getOutputStream();
            isWifiConnected = true;
            Log.i(TAG, "Connecté en Wifi à " + ip + ":" + port);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Erreur connexion Wifi", e);
            throw new Exception("Connexion Wifi échouée : " + e.getMessage());
        }
    }

    /**
     * Déconnecter Wifi
     */
    public void disconnectWifi() {
        try {
            if (wifiOutputStream != null) wifiOutputStream.close();
            if (wifiSocket != null) wifiSocket.close();
        } catch (Exception e) {
            Log.e(TAG, "Erreur disconnectWifi", e);
        }
        isWifiConnected = false;
        Log.i(TAG, "Déconnecté Wifi");
    }

    /**
     * Vérifier si Wifi est connecté
     */
    public boolean isWifiConnected() {
        return isWifiConnected;
    }





//    USB
    public JSArray listUsbDevices() {
        JSArray devicesArray = new JSArray();

        if (usbManager == null) {
            Log.e(TAG, "usbManager is null");
            return devicesArray;
        }

        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        for (UsbDevice device : deviceList.values()) {
            JSObject dev = new JSObject();
            dev.put("name", device.getDeviceName());
            dev.put("vendorId", device.getVendorId());
            dev.put("productId", device.getProductId());
            devicesArray.put(dev);
        }
        return devicesArray;
    }


    public boolean connectUsb(int vendorId, int productId) throws Exception {
//        if (!usbManager.hasPermission(targetDevice)) {
//            requestUsbPermission(targetDevice);
//            throw new Exception("Demande de permission USB envoyée, réessaie ensuite.");
//        }
        if (usbManager == null) throw new Exception("USB Manager non disponible.");

        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        UsbDevice targetDevice = null;

        for (UsbDevice device : deviceList.values()) {
            if (device.getVendorId() == vendorId && device.getProductId() == productId) {
                targetDevice = device;
                break;
            }
        }

        if (targetDevice == null) throw new Exception("USB device not found");

        if (!usbManager.hasPermission(targetDevice)) {
            throw new Exception("No permission to access USB device");
        }

        UsbDeviceConnection connection = usbManager.openDevice(targetDevice);
        if (connection == null) throw new Exception("Failed to open USB device");

        // Trouver l’endpoint de sortie (bulk OUT)
        UsbInterface usbInterface = targetDevice.getInterface(0);
        connection.claimInterface(usbInterface, true);

        UsbEndpoint endpointOut = null;
        for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
            UsbEndpoint ep = usbInterface.getEndpoint(i);
            if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK &&
                    ep.getDirection() == UsbConstants.USB_DIR_OUT) {
                endpointOut = ep;
                break;
            }
        }

        if (endpointOut == null) {
            connection.close();
            throw new Exception("No bulk OUT endpoint found");
        }

        this.usbConnection = connection;
        this.usbEndpoint = endpointOut;
        this.isUsbConnected = true;

        Log.i(TAG, "Connecté USB: vendorId=" + vendorId + " productId=" + productId);

        return true;
    }

    public void disconnectUsb() {
        try {
            if (usbConnection != null) {
                usbConnection.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during USB disconnect", e);
        }
        usbConnection = null;
        usbEndpoint = null;
        isUsbConnected = false;
        Log.i (TAG, "USB Déconnecté.");
    }


    public boolean isUsbConnected() {
        return isUsbConnected && usbConnection != null;
    }

    private final BroadcastReceiver usbPermissionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            Log.i(TAG, "Permission USB accordée pour l’appareil " + device.getDeviceName());
                            // Ici tu peux continuer ton connectUsb(device)
                        }
                    } else {
                        Log.w(TAG, "Permission USB refusée pour l’appareil " + device.getDeviceName());
                    }
                }
            }
        }
    };

    public void unregisterUsbPermissionReceiver() {
        try {
            context.unregisterReceiver(usbPermissionReceiver);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "usbPermissionReceiver déjà désenregistré ou non enregistré");
        }
    }





}
