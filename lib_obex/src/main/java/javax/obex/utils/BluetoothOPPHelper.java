package javax.obex.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.obex.ClientSession;
import javax.obex.HeaderSet;
import javax.obex.ObexTransport;
import javax.obex.Operation;
import javax.obex.ResponseCodes;

/**
 * OBEX(Object Exchange) Helper
 */
public class BluetoothOPPHelper {

    private final static String TAG = BluetoothOPPHelper.class.getSimpleName();

    private final UUID OPPUUID = UUID.fromString(("00001105-0000-1000-8000-00805f9b34fb"));
    private final static String RC_BLUETOOTH_DEVICE_NAME = "MyBlueDeviceName";

    /**
     * 使用 OBEX Object Push Profile 向对端推送文件
     *
     * @param filePath
     */
    public void pushOppFile(String filePath) {
        try {
            BluetoothDevice device = getBondedRCDevice();
            BluetoothSocket mBtSocket = device.createInsecureRfcommSocketToServiceRecord(OPPUUID);
//              BluetoothSocket mBtSocket = device.createRfcommSocketToServiceRecord(OPPUUID);

            ClientSession mClientSession = initClientSession(mBtSocket);

            byte[] fileBytes = generateFileBytes(filePath);
            String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);

            pushFile(mClientSession, fileBytes, fileName);

            //断开连接并关闭BtSocket
            Log.d(TAG, "disconnect clientSession");
            mClientSession.disconnect(null);
            TimeUnit.MILLISECONDS.sleep(500);
            Log.d(TAG, "close BtSocket");
            mBtSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "error " + e.getMessage());
        }
    }

    private BluetoothDevice getBondedRCDevice() {
        Set<BluetoothDevice> bondedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        bondedDevices.stream().forEach(dev -> Log.d(TAG, "device: " + dev.getName() + " -> " + dev.getAddress()));
        Optional<BluetoothDevice> deviceOptional = bondedDevices.stream()
//                .filter(dev -> dev.getName().contains(RC_BLUETOOTH_DEVICE_NAME))
                .findFirst();
        return deviceOptional.get();
    }

    private ClientSession initClientSession(BluetoothSocket btSocket) {
        Log.d(TAG, "init client session");
        ClientSession mSession = null;

        boolean retry = true;
        int times = 0;
        while (retry && times < 4) {
            try {
                btSocket.connect();
            } catch (Exception e) {
                Log.e(TAG, "connect btSocket error " + e.getMessage());
                retry = true;
                times++;
                continue;
            }
            try {
                BluetoothObexTransport mTransport = null;
                mSession = new ClientSession((ObexTransport) (mTransport = new BluetoothObexTransport(btSocket)));
                HeaderSet headerset = new HeaderSet();
                headerset = mSession.connect(null);
                if (headerset.getResponseCode() == ResponseCodes.OBEX_HTTP_OK) {
                    boolean mConnected = true;
                    Log.d(TAG, "session connect OBEX_HTTP_OK");
                } else {
                    Log.e(TAG, "SEnd by OPP denied");
                    mSession.disconnect(headerset);
                    times++;
                    continue;
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "init client session error " + e.getMessage());
                retry = true;
                times++;
                continue;
            }
            retry = false;
        }
        return mSession;
    }

    private boolean pushFile(ClientSession session, byte[] bytes, String filename) {
        Log.d(TAG, "pushFile fileName[" + filename + "], file length[" + bytes.length + "]");
        boolean retry = true;
        int times = 0;
        while (retry && times < 4) {
            Operation putOperation = null;
            OutputStream mOutput = null;
            try {
                // Send a file with meta data to the server
                final HeaderSet hs = new HeaderSet();
                hs.setHeader(HeaderSet.NAME, filename);
                hs.setHeader(HeaderSet.TYPE, Utility.getMimeType(filename));
                hs.setHeader(HeaderSet.LENGTH, new Long((long) bytes.length));
                putOperation = session.put(hs);

                mOutput = putOperation.openOutputStream();
                mOutput.write(bytes);
                mOutput.close();
                mOutput.flush();

                putOperation.close();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "pushFile failed" + e.getMessage());
                retry = true;
                times++;
                continue;
            } finally {
                try {
                    if (mOutput != null)
                        mOutput.close();
                    if (putOperation != null)
                        putOperation.close();
                } catch (Exception e) {
                    Log.e(TAG, "pushFile error " + e.getMessage());
                    retry = true;
                    times++;
                    continue;
                }
            }
            retry = false;
            return true;
        }
        return false;
    }

    private byte[] generateFileBytes(String filePath) {
        byte[] fileBytes = null;
        FileInputStream fis = null;
        try {
            File file = new File(filePath);
            fis = new FileInputStream(file);
            int length = fis.available();
            Log.d(TAG, "file length " + length);
            fileBytes = new byte[length];
            int readLen = fis.read(fileBytes);
            return fileBytes;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != fis) {
                try {
                    fis.close();
                    fis = null;
                } catch (IOException e) {
                    Log.e(TAG, "close file input stream error " + e.getMessage());
                }
            }
        }
        return fileBytes;
    }

    private class OPPBatchInfo {
        String as;
        String type;
        byte[] data;

        public OPPBatchInfo(String as, String type, byte[] data) {
            this.as = as;
            this.data = data;
            this.type = type;
        }
    }
}
