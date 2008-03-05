package com.sun.midp.installer;

/** Class to hold all emulated device information **/
    public class DeviceDesc {
        private static String name[] = null;
        private static int w[];
        private static int h[];
        private static DeviceDesc devices = null;

        private DeviceDesc() {};

        private static native String getDeviceName0(int id);
        private static native int getDeviceWidth0(int id);
        private static native int getDeviceHeight0(int id);
        private static native int getDevicesNumber0();
        private static native void setCurrentDevice0(int id);
        private static native int getDeviceKeyCode0(int id, int javacallkey);
        private static native int getCurrentDevice0();
        
        public static DeviceDesc initDevicesDesc() {
            if (devices != null) {
                return devices;
            }
            
            devices = new DeviceDesc();
            int n = getDevicesNumber0();
            devices.name = new String[n];
            devices.w = new int[n];
            devices.h = new int[n];
            int i;
            for (i = 0; i < n; i++) {
                devices.name[i] = getDeviceName0(i);
                devices.w[i] = getDeviceWidth0(i);
                devices.h[i] = getDeviceHeight0(i);
            }
            return devices;
        }

        public static int getDeviceWidth(int id) {
            if (id < devices.w.length && id >= 0) {
                return devices.w[id];
            } else {
                return 480;
            }
        }

        public static int getDeviceHeight(int id) {
            if (id < devices.h.length && id >= 0) {
                return devices.h[id];
            } else {
                return 272;
            }
        }

        public static String getDeviceName(int id) {
            if (id < devices.name.length && id >= 0) {
                return devices.name[id];
            } else {
                return null;
            }
        }

        public static int getDeviceId(String name) {
            int i;
            for (i = 0; i < devices.name.length; i++) {
                if (name.equals(devices.name[i])) {
                	return i;
                }
            }

            return -1;
        }

        public static void setCurrentDevice(int id) {
            System.out.println("setCurrentDevice:"+id);
            setCurrentDevice0(id);
        }

        public static int getDeviceKeyCode(int id, int javacallkey) {
            System.out.println("getDeviceKeyCode:"+id+" key:"+javacallkey);
            if (id == -1) {
                return javacallkey;
            }
            return getDeviceKeyCode0(id, javacallkey);
        }

        public static int getCurrentDevice() {
            return getCurrentDevice0();
        }
    }
