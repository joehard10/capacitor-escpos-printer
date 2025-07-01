package cg.sijit.escpos;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;

import java.io.OutputStream;
import java.net.Socket;
import java.util.Set;
import java.util.UUID;

/**
 * Implémentation réelle des fonctions Bluetooth ESC/POS
 */
public class ESCPOSPrinter {

    private static final String TAG = "ESCPOSPrinter";

    private Socket wifiSocket = null;
    private OutputStream wifiOutputStream = null;
    private boolean isWifiConnected = false;

    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothSocket socket = null;
    private OutputStream outputStream = null;
    private boolean isConnected = false;

    private Context context; // Nécessaire pour vérifier permissions

    public ESCPOSPrinter(Context context) {
        this.context = context;
    }

    /**
     * Vérifie la permission BLUETOOTH_CONNECT sur Android 12+
     */
    private boolean hasBluetoothConnectPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
        } else {
            // Pas besoin sur Android < 12
            return true;
        }
    }

    /**
     * Lister les appareils Bluetooth déjà appairés
     */
    public JSArray listPairedDevices() {
        JSArray devicesArray = new JSArray();

        if (bluetoothAdapter == null) {
            return devicesArray;
        }

        if (!hasBluetoothConnectPermission()) {
            Log.w(TAG, "No BLUETOOTH_CONNECT permission to list devices");
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
            Log.e(TAG, "SecurityException in listPairedDevices", e);
        }
        return devicesArray;
    }

    /**
     * Se connecter à une imprimante par nom
     */
    public boolean connect(String deviceName) throws Exception {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            throw new Exception("Bluetooth not available or disabled");
        }

        if (!hasBluetoothConnectPermission()) {
            throw new Exception("Missing BLUETOOTH_CONNECT permission");
        }

        BluetoothDevice printer = findDeviceByName(deviceName);
        if (printer == null) {
            throw new Exception("Device not found: " + deviceName);
        }

        try {
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // SPP
            socket = printer.createRfcommSocketToServiceRecord(uuid);
            socket.connect();
            outputStream = socket.getOutputStream();
            isConnected = true;
            return true;
        } catch (SecurityException se) {
            Log.e(TAG, "SecurityException during Bluetooth connect", se);
            throw new Exception("Bluetooth permission revoked or missing during connect");
        }
    }

    /**
     * Déconnecter proprement
     */
    public void disconnect() {
        try {
            if (outputStream != null) outputStream.close();
            if (socket != null) socket.close();
        } catch (Exception e) {
            Log.e(TAG, "Error during disconnect", e);
        }
        isConnected = false;
    }

    /**
     * Vérifier état connexion
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Imprimer du texte
     */
    public void printText(String text) throws Exception {
        if ((isConnected && outputStream != null)) {
            outputStream.write(text.getBytes("UTF-8"));
            outputStream.flush();
        } else if (isWifiConnected && wifiOutputStream != null) {
            wifiOutputStream.write(text.getBytes("UTF-8"));
            wifiOutputStream.flush();
        } else {
            throw new Exception("Not connected to any printer (Bluetooth or Wifi)");
        }
    }

    /**
     * Trouver un device appairé par nom
     */
    private BluetoothDevice findDeviceByName(String name) {
        if (bluetoothAdapter == null) return null;

        if (!hasBluetoothConnectPermission()) {
            Log.w(TAG, "No BLUETOOTH_CONNECT permission to find device");
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
            Log.e(TAG, "SecurityException in findDeviceByName", e);
            return null;
        }
        return null;
    }

    /**
     * Activer/désactiver le gras
     */
    public void setBold(boolean bold) throws Exception {
        if (!isConnected || outputStream == null) throw new Exception("Not connected");
        byte[] cmd = bold ? new byte[] { 0x1B, 0x45, 0x01 } : new byte[] { 0x1B, 0x45, 0x00 };
        outputStream.write(cmd);
        outputStream.flush();
    }

    /**
     * Aligner le texte
     * @param align "left", "center", "right"
     */
    public void setAlignment(String align) throws Exception {
        if (!isConnected || outputStream == null) throw new Exception("Not connected");
        byte alignByte;
        switch (align.toLowerCase()) {
            case "center": alignByte = 1; break;
            case "right": alignByte = 2; break;
            default: alignByte = 0; // left
        }
        byte[] cmd = new byte[] { 0x1B, 0x61, alignByte };
        outputStream.write(cmd);
        outputStream.flush();
    }

    /**
     * Changer la taille du texte
     * @param size: ex 1=normal, 2=double height, 3=double width & height
     */
    public void setTextSize(int size) throws Exception {
        if (!isConnected || outputStream == null) throw new Exception("Not connected");
        byte sizeByte = 0x00;
        switch (size) {
            case 2: sizeByte = 0x11; break; // double height & width
            case 3: sizeByte = 0x22; break; // triple (rare, dépend du modèle)
            default: sizeByte = 0x00; // normal
        }
        byte[] cmd = new byte[] { 0x1D, 0x21, sizeByte };
        outputStream.write(cmd);
        outputStream.flush();
    }

    /**
     * Couper le ticket (si imprimante supporte)
     */
    public void cutPaper() throws Exception {
        if (!isConnected || outputStream == null) throw new Exception("Not connected");
        byte[] cmd = new byte[] { 0x1D, 0x56, 0x00 }; // 0x00 = full cut
        outputStream.write(cmd);
        outputStream.flush();
    }

    /**
     * Connexion à une imprimante ESC/POS en Wifi (IP et port)
     */
    public boolean connectWifi(String ip, int port) throws Exception {
        if (ip == null || ip.isEmpty()) throw new Exception("IP is required");
        wifiSocket = new Socket(ip, port);
        wifiOutputStream = wifiSocket.getOutputStream();
        isWifiConnected = true;
        return true;
    }

    public void disconnectWifi() {
        try {
            if (wifiOutputStream != null) wifiOutputStream.close();
            if (wifiSocket != null) wifiSocket.close();
        } catch (Exception e) {
            Log.e(TAG, "Error during disconnectWifi", e);
        }
        isWifiConnected = false;
    }

    public boolean isWifiConnected() {
        return isWifiConnected;
    }
}
