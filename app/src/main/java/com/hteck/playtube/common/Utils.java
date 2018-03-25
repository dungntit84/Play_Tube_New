package com.hteck.playtube.common;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.hteck.playtube.R;
import com.hteck.playtube.activity.MainActivity;
import com.hteck.playtube.data.YoutubeInfo;
import com.hteck.playtube.view.LoadingView;

import org.json.JSONObject;
import org.w3c.dom.Document;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class Utils {

    public static void showMessage(final String msg) {
        MainActivity.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.getInstance(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

//    public static String getString(int resId) {
//        try {
//            if (resId == R.string.app_name) {
//                return MainActivity.getInstance().getString(resId);
//            }
//            return EncryptUtils.decrypt(MainActivity.getInstance().getString(resId));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return "";
//    }

    public static String getString(int resId) {
        try {
            return MainActivity.getInstance().getString(resId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean stringIsNullOrEmpty(String val) {
        return val == null || val.trim().equals("");
    }

    public static Document parseDoc(String xmlContent) {
        try {
            InputStream inputStream = new ByteArrayInputStream(
                    xmlContent.getBytes());

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(inputStream);
            doc.getDocumentElement().normalize();
            return doc;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String formatNumber(double number, boolean isShortDisplay) {
        if (isShortDisplay) {
            int oneBillion = 1000000000;
            if (number >= oneBillion) {
                int val = (int) (number / oneBillion);
                if (val < 10) {
                    int val1 = (int) (number / (oneBillion / 10));
                    return (float) val1 / 10 + "B";
                }
                return val + "B";
            }
            int oneMillion = 1000000;
            if (number >= oneMillion) {
                int val = (int) (number / oneMillion);
                if (val < 10) {
                    int val1 = (int) (number / (oneMillion / 10));
                    return (float) val1 / 10 + "M";
                }
                return val + "M";
            }
            int oneThousand = 1000;
            if (number >= oneThousand) {
                int val = (int) (number / oneThousand);
                if (val < 10) {
                    int val1 = (int) (number / (oneThousand / 10));
                    return (float) val1 / 10 + "K";
                }
                return val + "K";
            }
        }

        DecimalFormat myFormatter = new DecimalFormat("###,###,###,###");
        String output = myFormatter.format(number);

        return output;
    }

    public static String getDisplayViews(double viewsNo, boolean isMinDisplay) {
        try {
            return formatNumber(viewsNo, isMinDisplay)
                    + (viewsNo > 1 ? " views" : " view");
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return Double.toString(viewsNo);
    }

    public static String getDisplayLikes(double likesNo, boolean isMinDisplay) {
        return formatNumber(likesNo, isMinDisplay) + (likesNo > 1 ? " likes" : " like");
    }

    public static String formatNumber(double number) {
        DecimalFormat myFormatter = new DecimalFormat("###,###,###,###");
        String output = myFormatter.format(number);

        return output;
    }

    public static String getDisplayTime(int seconds) {
        try {
            int h = seconds / 3600;
            int m = (seconds % 3600) / 60;
            int s = seconds % 60;

            String text = null;

            if (h == 0)
                text = String.format("%02d:%02d", m, s);
            else
                text = String.format("%d:%02d:%02d", h, m, s);
            return text;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return Integer.toString(seconds);
    }

    public static int getPrefValue(String key, int defValue) {
        SharedPreferences pref = MainActivity.getInstance()
                .getSharedPreferences(Constants.PLAYTUBE_PREF,
                        MainActivity.MODE_PRIVATE);
        return pref.getInt(key, defValue);
    }

    public static String getPrefValue(String key, String defValue) {
        SharedPreferences pref = MainActivity.getInstance()
                .getSharedPreferences(Constants.PLAYTUBE_PREF,
                        MainActivity.MODE_PRIVATE);
        return pref.getString(key, defValue);
    }

    public static boolean getPrefValue(String key, boolean defValue) {
        SharedPreferences pref = MainActivity.getInstance()
                .getSharedPreferences(Constants.PLAYTUBE_PREF,
                        MainActivity.MODE_PRIVATE);
        return pref.getBoolean(key, defValue);
    }

    public static void savePref(String key, int value) {
        SharedPreferences pref = MainActivity.getInstance()
                .getSharedPreferences(Constants.PLAYTUBE_PREF,
                        MainActivity.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = pref.edit();

        prefsEditor.putInt(key, value);

        prefsEditor.commit();
    }

    public static void savePref(String key, boolean value) {
        SharedPreferences pref = MainActivity.getInstance()
                .getSharedPreferences(Constants.PLAYTUBE_PREF,
                        MainActivity.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = pref.edit();

        prefsEditor.putBoolean(key, value);

        prefsEditor.commit();
    }

    public static void savePref(String key, String value) {
        SharedPreferences pref = MainActivity.getInstance()
                .getSharedPreferences(Constants.PLAYTUBE_PREF,
                        MainActivity.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = pref.edit();

        prefsEditor.putString(key, value);

        prefsEditor.commit();
    }

    public static View createNoItemView(Activity activity, String msg) {
        ViewGroup layout = (ViewGroup) activity.getLayoutInflater().inflate(
                R.layout.no_item_view, null);

        TextView textView = (TextView) layout.findViewById(R.id.no_item_tv_msg);
        textView.setText(msg);
        return layout;
    }

    public static Object convertToObject(Class clazz, String input) {
        try {
            if (boolean.class == clazz)
                return Boolean.parseBoolean(input);
            if (byte.class == clazz)
                return Byte.parseByte(input);
            if (short.class == clazz)
                return Short.parseShort(input);
            if (int.class == clazz)
                return Integer.parseInt(input);
            if (long.class == clazz)
                return Long.parseLong(input);
            if (float.class == clazz)
                return Float.parseFloat(input);
            if (double.class == clazz)
                return Double.parseDouble(input);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return input;
    }

    public static String getString(JSONObject jObj, String... propNames) {
        try {
            JSONObject item = null;
            if (propNames.length == 1) {
                item = jObj;
            }
            for (int i = 0; i < propNames.length - 1; ++i) {
                JSONObject tmp = item == null ? jObj : item;
                if (tmp.has(propNames[i])) {
                    item = tmp.getJSONObject(propNames[i]);
                } else {
                    return "";
                }
            }
            if (item != null && item.has(propNames[propNames.length - 1])) {
                return item.getString(propNames[propNames.length - 1]);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    public static int getTimeInSeconds(String duration) {
        try {
            duration = duration.replaceAll("[PT]", "");
            if (!duration.contains("H")) {
                duration = "0H" + duration;
            }
            if (!duration.contains("M")) {
                int indexOfH = duration.indexOf('H');
                duration = duration.substring(0, indexOfH + 1) + "0M"
                        + duration.substring(indexOfH + 1);
            }

            if (!duration.contains("S")) {
                duration = duration + "0S";
            }
            duration = duration.replaceAll("[HMS]", " ");
            String[] elements = duration.trim().split("[ ]");
            if (elements.length == 1) {
                return parseInt(elements[0]);
            }
            if (elements.length == 2) {
                return parseInt(elements[0]) * 60
                        + parseInt(elements[1]);
            }
            if (elements.length == 3) {
                return parseInt(elements[0]) * 3600
                        + parseInt(elements[1]) * 60
                        + parseInt(elements[2]);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int getInt(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (Throwable e) {
            return defaultValue;
        }
    }

    public static int getInt(Object value) {
        try {
            return Integer.parseInt(value.toString());
        } catch (Throwable e) {
            return 0;
        }
    }

    public static int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Throwable e) {
            return 0;
        }
    }

    public static String getDisplayTime(String input) {
        DateFormat formatter = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        try {
            Date dt = formatter.parse(input);

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, calendar.getTime()
                    .getTimezoneOffset());
            Date dtNow = calendar.getTime();
            int diffInHour = (int) ((dtNow.getTime() - dt.getTime()) / (1000 * 60 * 60));

            if (diffInHour < 24) {
                return String.format("%s %s ago", diffInHour,
                        diffInHour > 1 ? "hours" : "hour");
            }
            int days = diffInHour / 24;
            if (days < 30) {
                return String.format("%s %s ago", days, days > 1 ? "days"
                        : "day");
            }
            int months = days / 30;
            if (months < 12) {
                return String.format("%s %s ago", months, months > 1 ? "months"
                        : "month");
            }

            int years = months / 12;
            return String.format("%s %s ago", years, years > 1 ? "years"
                    : "year");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static LoadingView showProgressBar(ViewGroup parent,
                                              LoadingView loadingView, boolean... visibleTitle) {
        try {
            if (loadingView == null) {
                loadingView = new LoadingView(MainActivity.getInstance(),
                        visibleTitle);
            }
            ViewGroup currentParent = (ViewGroup) loadingView.getParent();
            if (currentParent != null) {
                currentParent.removeView(loadingView);
            }
            parent.addView(loadingView);
            loadingView.bringToFront();
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return loadingView;
    }

    public static LoadingView hideProgressBar(ViewGroup parent, LoadingView loadingView) {
        try {
            if (loadingView != null) {
                parent.removeView(loadingView);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        loadingView = null;
        return loadingView;
    }

    public static void shareVideo(YoutubeInfo videoInfo) {
        try {

            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            String title = String.format(getString(R.string.share_video_title),
                    videoInfo.title);
            String content = String.format(
                    getString(R.string.share_video_content), videoInfo.id);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, title);
            sharingIntent.putExtra(Intent.EXTRA_TEXT, content);
            MainActivity.getInstance().startActivity(
                    Intent.createChooser(sharingIntent, MainActivity
                            .getInstance().getString(R.string.app_name)));

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static int getYoutubeWidth() {
        try {
            DisplayMetrics displaymetrics = new DisplayMetrics();
            MainActivity.getInstance().getWindowManager().getDefaultDisplay()
                    .getMetrics(displaymetrics);

            int width = displaymetrics.widthPixels - convertPointToPixels((int) MainActivity.getInstance().getResources().getDimension(R.dimen.row_padding_grid));
            int count = 1;
            while ((float) (displaymetrics.widthPixels / count) > convertPointToPixels(200)) {
                count++;
            }
            if (count == 1) {
                count = 2;
            }
            return width / count;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return 2;
    }

    public static int convertPointToPixels(int point) {
        try {
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    point, MainActivity.getInstance().getResources()
                            .getDisplayMetrics());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return point;
    }

    public static int getScreenWidth() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        MainActivity.getInstance().getWindowManager().getDefaultDisplay()
                .getMetrics(displaymetrics);
        return displaymetrics.widthPixels;
    }

    public static int getScreenWidthSmall() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        MainActivity.getInstance().getWindowManager().getDefaultDisplay()
                .getMetrics(displaymetrics);
        return displaymetrics.widthPixels > displaymetrics.heightPixels ? displaymetrics.heightPixels
                : displaymetrics.widthPixels;
    }

    public static int getScreenHeight() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        MainActivity.getInstance().getWindowManager().getDefaultDisplay()
                .getMetrics(displaymetrics);
        return displaymetrics.heightPixels;
    }

    public static int getPlayerHeightPortrait() {
        return 360 * getScreenWidth() / 640;
    }

    public static JSONObject getJSONObject(JSONObject jObj, String... propNames) {
        try {
            JSONObject result = null;
            for (int i = 0; i < propNames.length; ++i) {
                JSONObject tmp = result == null ? jObj : result;
                if (tmp.has(propNames[i])) {
                    result = tmp.getJSONObject(propNames[i]);
                } else {
                    return null;
                }
            }
            return result;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getDisplayDateTime(String input) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        DateFormat dateFormatDisplay = new SimpleDateFormat(
                "MMM-dd-yyyy HH:mm:ss");
        try {
            Date date = dateFormat.parse(input);
            return dateFormatDisplay.format(date);
        } catch (Throwable e) {
            e.printStackTrace();
            return input;
        }
    }

    public static Object toObject(Class clazz, JSONObject jObject, String propName) {
        try {
            if (boolean.class == clazz)
                return jObject.getBoolean(propName);
            if (int.class == clazz)
                return jObject.getInt(propName);
            if (long.class == clazz)
                return jObject.getLong(propName);
            if (float.class == clazz)
                return (float) jObject.getDouble(propName);
            if (double.class == clazz)
                return jObject.getDouble(propName);
            return jObject.getString(propName);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }
}
