package utilities;

import java.math.BigInteger;

/**
 * Created by Fujitsu on 29/2/2016.
 */
public class Convertor {
    public static String convertToHex(String map){
        //remove breakline carriage return
        map = map.replaceAll("\r","");
        map = map.replaceAll("\n","");
        String map_desc = "";
        for(int i=0;i<map.length();i+=4)
            map_desc+=new BigInteger(map.substring(i,i+4),2).toString(16);
        return map_desc;
    }

    public static String convertFromHex(String hex){
        hex = hex.replaceAll("\r","");
        hex = hex.replaceAll("\n","");
        String ret =  "";
        for(int i=0;i<hex.length();i++){
            String bits = new BigInteger(Character.toString(hex.charAt(i)),16).toString(2);
            //append back 0
            if(bits.length()!=4)
                for(int j=bits.length();j<4;j++)
                    bits = "0"+bits;
            ret+=bits;
        }
        return ret;
    }

    //super decimal is numbers based on 27, created by myself
    public static char toSuperDecimal(char[] ternary){

        int value = 9 * (ternary[0] - '0') + 3 * (ternary[1] - '0') + (ternary[2] - '0');
        if(value < 10)
            return (char) ('0' + value);
        else          //value is from 10 to 26
            return (char) ('a' + value - 10);
    }

    public static String fromObstacleInfo(String obstacleInfo){         //this method is meant to be used by android
        int i=0;
        String obstacleInfoWithSpace = "";
        for(; i< obstacleInfo.length()-1; i++){
            obstacleInfoWithSpace += fromSuperDecimal(obstacleInfo.charAt(i)) + " ";
        }
        obstacleInfoWithSpace += fromSuperDecimal(obstacleInfo.charAt(i));
        return obstacleInfoWithSpace;
    }

    public static String fromSuperDecimal(char superDecimal){           //this method is meant to be used by android
        char[] ternary = new char[5];
        int value;
        if(superDecimal <= '9')
            value = superDecimal - '0';
        else
            value = superDecimal - 'a' + 10;
        ternary[0] = (char) (value / 9 + '0');
        ternary[1] = ' ';
        ternary[2] = (char) ((value / 3) % 3 + '0');
        ternary[3] = ' ';
        ternary[4] = (char) (value % 3 + '0');
        return new String(ternary);
    }
}
