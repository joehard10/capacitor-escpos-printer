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
import com.getcapacitor.annotation.PermissionCallback;

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
                ),
//                @Permission(
//                        alias = "usb",
//                        strings = {
//                                Manifest.permission.USB_PERMISSION // ⚠️ À adapter, USB n’a pas toujours besoin de runtime permission mais tu peux déclarer un alias pour homogénéité
//                        }
//                ),
                @Permission(
                        alias = "wifi",
                        strings = {
                                Manifest.permission.ACCESS_WIFI_STATE,
                                Manifest.permission.CHANGE_WIFI_STATE
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
        if (!checkPermission(call, "bluetooth")) return;
        JSArray devices = implementation.listPairedDevices();
        JSObject ret = new JSObject();
        ret.put("devices", devices);
        call.resolve(ret);
    }

    @PluginMethod
    public void connect(PluginCall call) {
        if (!checkPermission(call, "bluetooth")) return;
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
        if (!checkPermission(call, "bluetooth")) return;
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
        if (!checkPermission(call, "bluetooth")) return;
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
        if (!checkPermission(call, "bluetooth")) return;
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
        if (!checkPermission(call, "bluetooth")) return;
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
        if (!checkPermission(call, "bluetooth")) return;
        try {
            implementation.cutPaper();
            call.resolve(new JSObject().put("success", true));
        } catch (Exception e) {
            call.reject("Failed to cut paper: " + e.getMessage());
        }
    }




    @PluginMethod
//    public void checkPermissions(PluginCall call) {
//        JSObject ret = new JSObject();
//        ret.put("bluetooth", getPermissionState("bluetooth").toString());
//        call.resolve(ret);
//    }
    private boolean checkPermission(PluginCall call, String alias) {
        PermissionState state = getPermissionState(alias);
        if (state != PermissionState.GRANTED) {
            call.reject(alias + " permission not granted");
            return false;
        }
        return true;
    }

    @PluginMethod
//    public void requestPermissions(PluginCall call) {
//        requestPermissionForAlias("bluetooth", call, "permissionsCallback");
//    }
    public void requestPermissions(PluginCall call) {
        String alias = call.getString("alias", "bluetooth");
        if (!alias.equals("bluetooth") && !alias.equals("wifi")) {
            call.reject("Invalid permission alias");
            return;
        }
        requestPermissionForAlias(alias, call, "permissionsCallback");
    }

    @PermissionCallback
//    private void permissionsCallback(PluginCall call) {
//        JSObject ret = new JSObject();
//        ret.put("bluetooth", getPermissionState("bluetooth").toString());
//        call.resolve(ret);
//    }
    private void permissionsCallback(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("bluetooth", getPermissionState("bluetooth").toString());
//        ret.put("usb", getPermissionState("usb").toString());
        ret.put("wifi", getPermissionState("wifi").toString());
        call.resolve(ret);
    }

    /**
     * Vérifier si la permission Bluetooth est accordée. Si non, reject et return false.
     */
//    private boolean checkBluetoothPermission(PluginCall call) {
//        PermissionState state = getPermissionState("bluetooth");
//        if (state != PermissionState.GRANTED) {
//            call.reject("Bluetooth permission not granted");
//            return false;
//        }
//        return true;
//    }










    // WIFI

    @PluginMethod
    public void connectWifi(PluginCall call) {

        if (!checkPermission(call, "wifi")) return;

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
    public void listUsbDevices(PluginCall call) {
        JSArray devices = implementation.listUsbDevices();
        JSObject ret = new JSObject();
        ret.put("devices", devices);
        call.resolve(ret);
    }

    @PluginMethod
    public void connectUsb(PluginCall call) {
        Integer vendorId = call.getInt("vendorId");
        Integer productId = call.getInt("productId");
        if (vendorId == null || productId == null) {
            call.reject("vendorId and productId are required");
            return;
        }
        try {
            boolean connected = implementation.connectUsb(vendorId, productId);
            call.resolve(new JSObject().put("connected", connected));
        } catch (Exception e) {
            call.reject("USB connection failed: " + e.getMessage());
        }
    }

    @PluginMethod
    public void disconnectUsb(PluginCall call) {
        implementation.disconnectUsb();
        call.resolve(new JSObject().put("disconnected", true));
    }

    @PluginMethod
    public void isUsbConnected(PluginCall call) {
        boolean connected = implementation.isUsbConnected();
        call.resolve(new JSObject().put("connected", connected));
    }

}
