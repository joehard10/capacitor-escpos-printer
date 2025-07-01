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

/**
 * Plugin Capacitor pour imprimer sur imprimantes ESC/POS via Bluetooth.
 */
@CapacitorPlugin(
        name = "ESCPOSPrinter",
        permissions= {
                @com.getcapacitor.annotation.Permission(
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
    private ESCPOSPrinter implementation = new ESCPOSPrinter(getContext());


    /**
     * Lister les appareils Bluetooth appairés
     */
    @PluginMethod
    public void listPairedDevices(PluginCall call) {
        JSArray devices = implementation.listPairedDevices();
        JSObject ret = new JSObject();
        ret.put("devices", devices);
        call.resolve(ret);
    }

    /**
     * Se connecter à une imprimante par son nom
     */
    @PluginMethod
    public void connect(PluginCall call) {
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

    /**
     * Déconnexion propre
     */
    @PluginMethod
    public void disconnect(PluginCall call) {
        implementation.disconnect();
        call.resolve(new JSObject().put("disconnected", true));
    }

    /**
     * Vérifier si connecté
     */
    @PluginMethod
    public void isConnected(PluginCall call) {
        boolean connected = implementation.isConnected();
        call.resolve(new JSObject().put("connected", connected));
    }

    /**
     * Impression texte
     */
    @PluginMethod
    public void printText(PluginCall call) {
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

    /**
     * Vérifier permissions
     */
    @PluginMethod
    public void checkPermissions(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("bluetooth", getPermissionState("bluetooth").toString());
        call.resolve(ret);
    }

    /**
     * Demander permissions
     */
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

    @PluginMethod
    public void setBold(PluginCall call) {
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
        int port = call.getInt("port", 9100); // default port
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




}
