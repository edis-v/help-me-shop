package io.moxd.shopforme.utils

import java.util.regex.Pattern

val PASSWORD_PATTERN =
        Pattern.compile("^" +
                "(?=.*[0-9])" +         //at least 1 digit
                "(?=.*[a-z])" +         //at least 1 lower case letter
                "(?=.*[A-Z])" +         //at least 1 upper case letter
                "(?=.*[a-zA-Z])" +      //any letter
                "(?=\\S+$)" +           //no white spaces
                ".{4,}" +               //at least 4 characters
                "$");