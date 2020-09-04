package com.dappergeek0.applibrary;

import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by DapperGeek0 on 2/16/2017.
 */

public class StringHandler {

   // Check for valid email entry
   public boolean isEmailValid(String emailStr) {
      // Regular email address pattern
      Pattern VALID_EMAIL_ADDRESS_REGEX =
          Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
      // Match email address
      Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(emailStr);
      return matcher.find();
   }

   // Check for valid brand name format
   public static boolean isBrandName(String brandName){
      return brandName.matches("[a-zA-Z 0-9]+");
   }

   // Check for valid username entry
   public boolean isUsernameValid(String username){
      return username.matches("[a-zA-Z_0-9]+");
   }

   // Check for valid password entry
   public boolean isPasswordValid(String password) {
      return password.length() > 4 && !password.contains(" ");
   }

   // Check for text entry
   public static boolean isValidTextInput(String textInput){
      return textInput.matches("^[a-zA-Z_\\-&0-9., \\n()]+$");
   }


   /**
    * check for valid phone number input
    * @param phone
    * @return
    */
   public static boolean isPhoneValid(String phone) {
      //TODO: in the case of error processing, amend if statement
      //verify phone number length and only digit
      return (phone.length()>6 && TextUtils.isDigitsOnly(phone));
   }

   // Method capitalizes first letter of string
   public static String capFirst(String capString) {

      return capString.substring(0,1).toUpperCase()+capString.substring(1).toLowerCase();
   }

   // Capitalizes first letter of each word
   public static String capWords(String capAll) {
      String capitalWords = "";

      String[] splitWords = capAll.split("\\s+");

      // If length is more than a word, capitalize each word
      if (splitWords.length > 1) {
         for (String str : splitWords) {
            capitalWords += str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase() + " ";
         }
      }

      // else return the one word capitalized
      else capitalWords = capFirst(capAll);

      return capitalWords.trim();
   }

   // Gets first x characters of a string
   public static String shortenString(String str) {
      String summary;
      String newString;
      int last;
      // remove line breaks
      summary = str.replaceAll("[\\n|\\r]"," ");

      // Check string length
      if (summary.length() > 80) {
         // Get first hundred xters
         newString = summary.substring(0,80);

         // Get position of last space
         last = newString.lastIndexOf(" ");

         //
         summary = str.substring(0,last) + "..." ;
         summary = summary.replaceAll("[\\n|\\r]", " ");
      }

      // return summary string
      return summary;
   }

   // app play store url
   public static String appUrl() {
      return "https://play.google.com/store/apps/details?id=com.dappergeek0.uplanit";
   }

   // share app message
   public static String shareAppMessage() {
       return "Hi! check out uPlanIt to plan your events and promote your"
           + " business brand. Download at " + appUrl();
   }

   // Create basic share event message
   public static String shareBrandMessage(String brandName, String category){

      return  "Check out '" + brandName + "' on uPlanIt"
                + "\nService category: " + category
               + "\nTheir services can come in handy."
               + "\n\nShared with uPlanIt"
               + "\nDownload at " + appUrl();
   }

   // Create basic share event message
   public static String shareEventMessage(String eventName, String startDate, String startTime, String location, String details){

      return  "You're invited to " + eventName
                + "\non " + startDate + " " + startTime
                + " \nLocation: " + location
                + "\nAdded details: " + details
               + "\n\nEvent planned with uPlanIt"
               + "\nDownload at " + appUrl();
   }

   // Get the brand category from the brand Id
   public static String brandCategory(String categoryId, String[] categories) {

      String category = null;

      for (String str : categories) {

         // split the array elements by the equality '=' symbol
         String[] splits = str.split("=");
         String cId = splits[0];
         String cTitle = splits[1];

         if (cId.equals(categoryId)) {
            category = cTitle;
         }
      }

      return category;
   }

   public static String sha1Hash( String toHash )
   {
      String hash = null;
      try
      {
         MessageDigest digest = MessageDigest.getInstance( "SHA-1" );
         byte[] bytes = toHash.getBytes("UTF-8");
         digest.update(bytes, 0, bytes.length);
         bytes = digest.digest();

         // This is ~55x faster than looping and String.formating()
         hash = bytesToHex( bytes );
      }
      catch( NoSuchAlgorithmException e )
      {
         e.printStackTrace();
      }
      catch( UnsupportedEncodingException e )
      {
         e.printStackTrace();
      }
      return hash;
   }

   // http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
   final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
   public static String bytesToHex( byte[] bytes )
   {
      char[] hexChars = new char[ bytes.length * 2 ];
      for( int j = 0; j < bytes.length; j++ )
      {
         int v = bytes[ j ] & 0xFF;
         hexChars[ j * 2 ] = hexArray[ v >>> 4 ];
         hexChars[ j * 2 + 1 ] = hexArray[ v & 0x0F ];
      }
      return new String( hexChars );
   }

   // replace commas in string with underscored, used mainly for the details sent from brands listing screen to the brand details screen
   public static String replaceComma(String string){
      return string.replace(",", "_");
   }

   // revert commas replaced, used mainly in the brand details screen
   public static String revertComma(String string){
      return string.replace("_", ",");
   }
}