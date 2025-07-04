package cg.sijit.escpos;

import android.Manifest;
import android.util.Log;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;

/**
 * Plugin Capacitor pour imprimer sur imprimantes ESC/POS via Bluetooth et Wifi.
 */
@CapacitorPlugin(
        name = "ESCPOSPrinter",
        permissions = {
                @Permission(
                        alias = "bluetooth",
                        strings = {
                                Manifest.permission.BLUETOOTH,
                                Manifest.permission.BLUETOOTH_ADMIN,
                                Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.BLUETOOTH_SCAN
                        }
                )
        }
)
public class ESCPOSPrinterPlugin extends Plugin {

    private static final String TAG = "ESCPOSPrinterPlugin";
    private ESCPOSPrinter implementation;

    @Override
    public void load() {
        // Important : getContext() n’est pas dispo dans le constructeur, il faut le faire ici
        implementation = new ESCPOSPrinter(getContext());
    }

    @PluginMethod
    public void listPairedDevices(PluginCall call) {
        if (!checkBluetoothPermission(call)) return;
        JSArray devices = implementation.listPairedDevices();
        JSObject ret = new JSObject();
        ret.put("devices", devices);
        call.resolve(ret);
    }

    @PluginMethod
    public void connect(PluginCall call) {
        if (!checkBluetoothPermission(call)) return;
        String deviceName = call.getString("deviceName");
        if (deviceName == null) {
            call.reject("deviceName is required");
            return;
        }
        try {
            boolean connected = implementation.connect(deviceName);
            call.resolve(new JSObject().put("connected", connected));
        } catch (Exception e) {
            Log.e(TAG, "Connection failed", e);
            call.reject("Connection failed: " + e.getMessage());
        }
    }

    @PluginMethod
    public void disconnect(PluginCall call) {
        implementation.disconnect();
        call.resolve(new JSObject().put("disconnected", true));
    }

    @PluginMethod
    public void isConnected(PluginCall call) {
        boolean connected = implementation.isConnected();
        call.resolve(new JSObject().put("connected", connected));
    }

    @PluginMethod
    public void printText(PluginCall call) {
        if (!checkBluetoothPermission(call)) return;
        String text = call.getString("text");
        if (text == null) {
            call.reject("text is required");
            return;
        }
        try {
            implementation.printText(text);
            call.resolve(new JSObject().put("success", true));
        } catch (Exception e) {
            Log.e(TAG, "Print failed", e);
            call.reject("Print failed: " + e.getMessage());
        }
    }

    @PluginMethod
    public void setBold(PluginCall call) {
        if (!checkBluetoothPermission(call)) return;
        Boolean bold = call.getBoolean("bold", false);
        try {
            implementation.setBold(bold);
            call.resolve(new JSObject().put("success", true));
        } catch (Exception e) {
            call.reject("Failed to set bold: " + e.getMessage());
        }
    }

    @PluginMethod
    public void setAlignment(PluginCall call) {
        if (!checkBluetoothPermission(call)) return;
        String align = call.getString("align", "left");
        try {
            implementation.setAlignment(align);
            call.resolve(new JSObject().put("success", true));
        } catch (Exception e) {
            call.reject("Failed to set alignment: " + e.getMessage());
        }
    }

    @PluginMethod
    public void setTextSize(PluginCall call) {
        if (!checkBluetoothPermission(call)) return;
        int size = call.getInt("size", 1);
        try {
            implementation.setTextSize(size);
            call.resolve(new JSObject().put("success", true));
        } catch (Exception e) {
            call.reject("Failed to set text size: " + e.getMessage());
        }
    }

    @PluginMethod
    public void cutPaper(PluginCall call) {
        if (!checkBluetoothPermission(call)) return;
        try {
            implementation.cutPaper();
            call.resolve(new JSObject().put("success", true));
        } catch (Exception e) {
            call.reject("Failed to cut paper: " + e.getMessage());
        }
    }

    @PluginMethod
    public void connectWifi(PluginCall call) {
        String ip = call.getString("ip");
        int port = call.getInt("port", 9100);
        if (ip == null) {
            call.reject("ip is required");
            return;
        }
        try {
            boolean connected = implementation.connectWifi(ip, port);
            call.resolve(new JSObject().put("connected", connected));
        } catch (Exception e) {
            call.reject("Wifi connection failed: " + e.getMessage());
        }
    }

    @PluginMethod
    public void disconnectWifi(PluginCall call) {
        implementation.disconnectWifi();
        call.resolve(new JSObject().put("disconnected", true));
    }

    @PluginMethod
    public void isWifiConnected(PluginCall call) {
        boolean connected = implementation.isWifiConnected();
        call.resolve(new JSObject().put("connected", connected));
    }

    @PluginMethod
    public void checkPermissions(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("bluetooth", getPermissionState("bluetooth").toString());
        call.resolve(ret);
    }

    @PluginMethod
    public void requestPermissions(PluginCall call) {
        requestPermissionForAlias("bluetooth", call, "permissionsCallback");
    }

    @PluginMethod
    public void permissionsCallback(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("bluetooth", getPermissionState("bluetooth").toString());
        call.resolve(ret);
    }

    /**
     * Vérifier si la permission Bluetooth est accordée. Si non, reject et return false.
     */
    private boolean checkBluetoothPermission(PluginCall call) {
        PermissionState state = getPermissionState("bluetooth");
        if (state != PermissionState.GRANTED) {
            call.reject("Bluetooth permission not granted");
            return false;
        }
        return true;
    }
}
