package com.example.harsh.intheflow;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

public class PermissionHandler {

    public static boolean permissionReport;

    public static boolean requestLocationPermission(){
        Dexter.withActivity(MapsActivity.mapsActivity).withPermissions(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                // Use the report object here to check if the permissions are granted or not
                if (report.areAllPermissionsGranted()){
                    permissionReport = true;
                }

                if (report.isAnyPermissionPermanentlyDenied()){
                    permissionReport = false;
                }

            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                // This method is required if the user has not yet permanently denied permission
                // Here the key will be used to continue usage of permissions
                token.continuePermissionRequest();
            }
        }).withErrorListener(new PermissionRequestErrorListener() {
            @Override
            public void onError(DexterError error) {
                Toast.makeText(MapsActivity.mapsActivity,"There seems to be some error while " +
                        "managing location permissions",Toast.LENGTH_LONG).show();
            }
        }).onSameThread().check();

        return permissionReport;
    }

}
