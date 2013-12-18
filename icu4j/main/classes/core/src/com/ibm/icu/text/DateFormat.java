/*
 *   Copyright (C) 1996-2013, International Business Machines
 *   Corporation and others.  All Rights Reserved.
 */

package com.ibm.icu.text;

import java.io.InvalidObjectException;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;

import com.ibm.icu.impl.ICUResourceBundle;
import com.ibm.icu.impl.RelativeDateFormat;
import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.GregorianCalendar;
import com.ibm.icu.util.TimeZone;
import com.ibm.icu.util.ULocale;
import com.ibm.icu.util.ULocale.Category;

/**
 * {@icuenhanced java.text.DateFormat}.{@icu _usage_}
 * 
 * <p>
 * DateFormat is an abstract class for date/time formatting subclasses which formats and parses dates or time in a
 * language-independent manner. The date/time formatting subclass, such as SimpleDateFormat, allows for formatting
 * (i.e., date -> text), parsing (text -> date), and normalization. The date is represented as a <code>Date</code>
 * object or as the milliseconds since January 1, 1970, 00:00:00 GMT.
 * 
 * <p>
 * DateFormat helps you to format and parse dates for any locale. Your code can be completely independent of the locale
 * conventions for months, days of the week, or even the calendar format: lunar vs. solar. It provides many class
 * methods for obtaining default date/time formatters based on the default for a given locale and a number of formatting
 * styles or arbitrary "skeletons".
 * <ol>
 * <li>The formatting styles include FULL, LONG, MEDIUM, and SHORT. More detail and examples of using these styles are
 * provided in the method descriptions.
 * <li>The formatting styles only cover a fraction of the necessary usage. You often need to have just certain
 * combinations of fields, like Month and Year, but have it to be formatted appropriate to a given locale. This is done
 * using the (misnamed) getPatternInstance() method, supplying a skeleton. There are a number of constants that have
 * common pre-defined skeletons, such as {@link #MINUTE_SECOND} for something like "13:45" or {@link #YEAR_ABBR_MONTH}
 * for something like "Sept 2012".
 * </ol>
 * 
 * <p>
 * To format a date for the current Locale, use one of the static factory methods:
 * 
 * <pre>
 * myString = DateFormat.getDateInstance().format(myDate);
 * myString = DateFormat.getPatternInstance(DateFormat.YEAR_ABBR_MONTH).format(myDate);
 * </pre>
 * <p>
 * If you are formatting multiple numbers, it is more efficient to get the format and use it multiple times so that the
 * system doesn't have to fetch the information about the local language and country conventions multiple times.
 * 
 * <pre>
 * DateFormat df = DateFormat.getDateInstance();
 * for (int i = 0; i &lt; a.length; ++i) {
 *     output.println(df.format(myDate[i]) + &quot;; &quot;);
 * }
 * </pre>
 * <p>
 * To format a date for a different Locale, specify it in the call to getDateInstance().
 * 
 * <pre>
 * DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.FRANCE);
 * </pre>
 * <p>
 * You can use a DateFormat to parse also.
 * 
 * <pre>
 * myDate = df.parse(myString);
 * </pre>
 * <p>
 * There are many static factory methods available. Use getDateInstance to get the normal date format for that country.
 * Use getTimeInstance to get the time format for that country. Use getDateTimeInstance to get a date and time format.
 * You can pass in different options to these factory methods to control the length of the result; from SHORT to MEDIUM
 * to LONG to FULL. The exact result depends on the locale, but generally:
 * <ul>
 * <li>SHORT is completely numeric, such as 12.13.52 or 3:30pm
 * <li>MEDIUM is longer, such as Jan 12, 1952
 * <li>LONG is longer, such as January 12, 1952 or 3:30:32pm
 * <li>FULL is pretty completely specified, such as Tuesday, April 12, 1952 AD or 3:30:42pm PST.
 * </ul>
 * 
 * <p>
 * Use getPatternInstance to format with a skeleton. Typically this is with a predefined skeleton, like
 * {@link #YEAR_ABBR_MONTH} for something like "Sept 2012". If you don't want to use one of the predefined skeletons,
 * you can supply your own. The skeletons are like the patterns in SimpleDateFormat, except they:
 * <ol>
 * <li>only keep the field pattern letter and ignore all other parts in a pattern, such as space, punctuation, and
 * string literals.
 * <li>are independent of the order of fields.
 * <li>ignore certain differences in the field's pattern letter length:
 * <ol>
 * <li>For those non-digit calendar fields, the pattern letter length is important, such as MMM, MMMM, and MMMMM; E and
 * EEEE, and the field's pattern letter length is honored.
 * <li>For the digit calendar fields, such as M or MM, d or dd, yy or yyyy, the field pattern length is ignored and the
 * best match, which is defined in date time patterns, will be returned without honor the field pattern letter length in
 * skeleton.
 * </ol>
 * </ol>
 * 
 * <p>
 * You can also set the time zone on the format if you wish. If you want even more control over the format or parsing,
 * (or want to give your users more control), you can try casting the DateFormat you get from the factory methods to a
 * SimpleDateFormat. This will work for the majority of countries; just remember to put it in a try block in case you
 * encounter an unusual one.
 * 
 * <p>
 * You can also use forms of the parse and format methods with ParsePosition and FieldPosition to allow you to
 * <ul>
 * <li>progressively parse through pieces of a string.
 * <li>align any particular field, or find out where it is for selection on the screen.
 * </ul>
 * 
 * <h4>Synchronization</h4>
 * 
 * Date formats are not synchronized. It is recommended to create separate format instances for each thread. If multiple
 * threads access a format concurrently, it must be synchronized externally.
 * 
 * @see UFormat
 * @see NumberFormat
 * @see SimpleDateFormat
 * @see com.ibm.icu.util.Calendar
 * @see com.ibm.icu.util.GregorianCalendar
 * @see com.ibm.icu.util.TimeZone
 * @author Mark Davis, Chen-Lieh Huang, Alan Liu
 * @stable ICU 2.0
 */
public abstract class DateFormat extends UFormat {

    /**
     * The calendar that <code>DateFormat</code> uses to produce the time field
     * values needed to implement date and time formatting.  Subclasses should
     * initialize this to a calendar appropriate for the locale associated with
     * this <code>DateFormat</code>.
     * @serial
     * @stable ICU 2.0
     */
    protected Calendar calendar;

    /**
     * The number formatter that <code>DateFormat</code> uses to format numbers
     * in dates and times.  Subclasses should initialize this to a number format
     * appropriate for the locale associated with this <code>DateFormat</code>.
     * @serial
     * @stable ICU 2.0
     */
    protected NumberFormat numberFormat;

    /**
     * FieldPosition selector for 'G' field alignment,
     * corresponding to the {@link Calendar#ERA} field.
     * @stable ICU 2.0
     */
    public final static int ERA_FIELD = 0;

    /**
     * FieldPosition selector for 'y' field alignment,
     * corresponding to the {@link Calendar#YEAR} field.
     * @stable ICU 2.0
     */
    public final static int YEAR_FIELD = 1;

    /**
     * FieldPosition selector for 'M' field alignment,
     * corresponding to the {@link Calendar#MONTH} field.
     * @stable ICU 2.0
     */
    public final static int MONTH_FIELD = 2;

    /**
     * FieldPosition selector for 'd' field alignment,
     * corresponding to the {@link Calendar#DATE} field.
     * @stable ICU 2.0
     */
    public final static int DATE_FIELD = 3;

    /**
     * FieldPosition selector for 'k' field alignment,
     * corresponding to the {@link Calendar#HOUR_OF_DAY} field.
     * HOUR_OF_DAY1_FIELD is used for the one-based 24-hour clock.
     * For example, 23:59 + 01:00 results in 24:59.
     * @stable ICU 2.0
     */
    public final static int HOUR_OF_DAY1_FIELD = 4;

    /**
     * FieldPosition selector for 'H' field alignment,
     * corresponding to the {@link Calendar#HOUR_OF_DAY} field.
     * HOUR_OF_DAY0_FIELD is used for the zero-based 24-hour clock.
     * For example, 23:59 + 01:00 results in 00:59.
     * @stable ICU 2.0
     */
    public final static int HOUR_OF_DAY0_FIELD = 5;

    /**
     * FieldPosition selector for 'm' field alignment,
     * corresponding to the {@link Calendar#MINUTE} field.
     * @stable ICU 2.0
     */
    public final static int MINUTE_FIELD = 6;

    /**
     * FieldPosition selector for 's' field alignment,
     * corresponding to the {@link Calendar#SECOND} field.
     * @stable ICU 2.0
     */
    public final static int SECOND_FIELD = 7;

    /**
     * {@icu} FieldPosition selector for 'S' field alignment,
     * corresponding to the {@link Calendar#MILLISECOND} field.
     *
     * Note: Time formats that use 'S' can display a maximum of three
     * significant digits for fractional seconds, corresponding to millisecond
     * resolution and a fractional seconds sub-pattern of SSS. If the
     * sub-pattern is S or SS, the fractional seconds value will be truncated
     * (not rounded) to the number of display places specified. If the
     * fractional seconds sub-pattern is longer than SSS, the additional
     * display places will be filled with zeros.
     * @stable ICU 3.0
     */
    public final static int FRACTIONAL_SECOND_FIELD = 8;

    /**
     * Alias for FRACTIONAL_SECOND_FIELD.
     * @stable ICU 3.0
     */
    public final static int MILLISECOND_FIELD = FRACTIONAL_SECOND_FIELD;

    /**
     * FieldPosition selector for 'E' field alignment,
     * corresponding to the {@link Calendar#DAY_OF_WEEK} field.
     * @stable ICU 2.0
     */
    public final static int DAY_OF_WEEK_FIELD = 9;

    /**
     * FieldPosition selector for 'D' field alignment,
     * corresponding to the {@link Calendar#DAY_OF_YEAR} field.
     * @stable ICU 2.0
     */
    public final static int DAY_OF_YEAR_FIELD = 10;

    /**
     * FieldPosition selector for 'F' field alignment,
     * corresponding to the {@link Calendar#DAY_OF_WEEK_IN_MONTH} field.
     * @stable ICU 2.0
     */
    public final static int DAY_OF_WEEK_IN_MONTH_FIELD = 11;

    /**
     * FieldPosition selector for 'w' field alignment,
     * corresponding to the {@link Calendar#WEEK_OF_YEAR} field.
     * @stable ICU 2.0
     */
    public final static int WEEK_OF_YEAR_FIELD = 12;

    /**
     * FieldPosition selector for 'W' field alignment,
     * corresponding to the {@link Calendar#WEEK_OF_MONTH} field.
     * @stable ICU 2.0
     */
    public final static int WEEK_OF_MONTH_FIELD = 13;

    /**
     * FieldPosition selector for 'a' field alignment,
     * corresponding to the {@link Calendar#AM_PM} field.
     * @stable ICU 2.0
     */
    public final static int AM_PM_FIELD = 14;

    /**
     * FieldPosition selector for 'h' field alignment,
     * corresponding to the {@link Calendar#HOUR} field.
     * HOUR1_FIELD is used for the one-based 12-hour clock.
     * For example, 11:30 PM + 1 hour results in 12:30 AM.
     * @stable ICU 2.0
     */
    public final static int HOUR1_FIELD = 15;

    /**
     * FieldPosition selector for 'K' field alignment,
     * corresponding to the {@link Calendar#HOUR} field.
     * HOUR0_FIELD is used for the zero-based 12-hour clock.
     * For example, 11:30 PM + 1 hour results in 00:30 AM.
     * @stable ICU 2.0
     */
    public final static int HOUR0_FIELD = 16;

    /**
     * FieldPosition selector for 'z' field alignment,
     * corresponding to the {@link Calendar#ZONE_OFFSET} and
     * {@link Calendar#DST_OFFSET} fields.
     * @stable ICU 2.0
     */
    public final static int TIMEZONE_FIELD = 17;

    /**
     * {@icu} FieldPosition selector for 'Y' field alignment,
     * corresponding to the {@link Calendar#YEAR_WOY} field.
     * @stable ICU 3.0
     */
    public final static int YEAR_WOY_FIELD = 18;

    /**
     * {@icu} FieldPosition selector for 'e' field alignment,
     * corresponding to the {@link Calendar#DOW_LOCAL} field.
     * @stable ICU 3.0
     */
    public final static int DOW_LOCAL_FIELD = 19;

    /**
     * {@icu} FieldPosition selector for 'u' field alignment,
     * corresponding to the {@link Calendar#EXTENDED_YEAR} field.
     * @stable ICU 3.0
     */
    public final static int EXTENDED_YEAR_FIELD = 20;

    /**
     * {@icu} FieldPosition selector for 'g' field alignment,
     * corresponding to the {@link Calendar#JULIAN_DAY} field.
     * @stable ICU 3.0
     */
    public final static int JULIAN_DAY_FIELD = 21;

    /**
     * {@icu} FieldPosition selector for 'A' field alignment,
     * corresponding to the {@link Calendar#MILLISECONDS_IN_DAY} field.
     * @stable ICU 3.0
     */
    public final static int MILLISECONDS_IN_DAY_FIELD = 22;

    /**
     * {@icu} FieldPosition selector for 'Z' field alignment,
     * corresponding to the {@link Calendar#ZONE_OFFSET} and
     * {@link Calendar#DST_OFFSET} fields.
     * @stable ICU 3.0
     */
    public final static int TIMEZONE_RFC_FIELD = 23;

    /**
     * {@icu} FieldPosition selector for 'v' field alignment,
     * corresponding to the {@link Calendar#ZONE_OFFSET} and
     * {@link Calendar#DST_OFFSET} fields.  This displays the generic zone
     * name, if available.
     * @stable ICU 3.4
     */
    public final static int TIMEZONE_GENERIC_FIELD = 24;

    /**
     * {@icu} FieldPosition selector for 'c' field alignment,
     * corresponding to the {@link Calendar#DAY_OF_WEEK} field.
     * This displays the stand alone day name, if available.
     * @stable ICU 3.4
     */
    public final static int STANDALONE_DAY_FIELD = 25;

    /**
     * {@icu} FieldPosition selector for 'L' field alignment,
     * corresponding to the {@link Calendar#MONTH} field.
     * This displays the stand alone month name, if available.
     * @stable ICU 3.4
     */
    public final static int STANDALONE_MONTH_FIELD = 26;

    /**
     * {@icu} FieldPosition selector for 'Q' field alignment,
     * corresponding to the {@link Calendar#MONTH} field.
     * This displays the quarter.
     * @stable ICU 3.6
     */
    public final static int QUARTER_FIELD = 27;

    /**
     * {@icu} FieldPosition selector for 'q' field alignment,
     * corresponding to the {@link Calendar#MONTH} field.
     * This displays the stand alone quarter, if available.
     * @stable ICU 3.6
     */
    public final static int STANDALONE_QUARTER_FIELD = 28;

    /**
     * {@icu} FieldPosition selector for 'V' field alignment,
     * corresponding to the {@link Calendar#ZONE_OFFSET} and
     * {@link Calendar#DST_OFFSET} fields.  This displays the fallback timezone
     * name when VVVV is specified, and the short standard or daylight
     * timezone name ignoring commonlyUsed when a single V is specified.
     * @stable ICU 3.8
     */
    public final static int TIMEZONE_SPECIAL_FIELD = 29;

    /**
     * {@icu} FieldPosition selector for 'U' field alignment,
     * corresponding to the {@link Calendar#YEAR} field.
     * This displays the cyclic year name, if available.
     * @stable ICU 49
     */
    public final static int YEAR_NAME_FIELD = 30;

    /**
     * {@icu} FieldPosition selector for 'O' field alignment,
     * corresponding to the {@link Calendar#ZONE_OFFSET} and
     * {@link Calendar#DST_OFFSET} fields.  This displays the
     * localized GMT format.
     * @draft ICU 51
     * @provisional This API might change or be removed in a future release.
     */
    public final static int TIMEZONE_LOCALIZED_GMT_OFFSET_FIELD = 31;

    /**
     * {@icu} FieldPosition selector for 'X' field alignment,
     * corresponding to the {@link Calendar#ZONE_OFFSET} and
     * {@link Calendar#DST_OFFSET} fields.  This displays the
     * ISO 8601 local time offset format or UTC indicator ("Z").
     * @draft ICU 51
     * @provisional This API might change or be removed in a future release.
     */
    public final static int TIMEZONE_ISO_FIELD = 32;

    /**
     * {@icu} FieldPosition selector for 'x' field alignment,
     * corresponding to the {@link Calendar#ZONE_OFFSET} and
     * {@link Calendar#DST_OFFSET} fields.  This displays the
     * ISO 8601 local time offset format.
     * @draft ICU 51
     * @provisional This API might change or be removed in a future release.
     */
    public final static int TIMEZONE_ISO_LOCAL_FIELD = 33;

    /**
     * {@icu} Number of FieldPosition selectors for DateFormat.
     * Valid selectors range from 0 to FIELD_COUNT-1.
     * @stable ICU 3.0
     */

    public final static int FIELD_COUNT = 34; // must == DateFormatSymbols.patternChars.length()

    
    /**
     * boolean attributes
     * <br/>
     * PARSE_ALLOW_WHITESPACE - indicates whitespace tolerance. Also included is trailing dot tolerance.
     * <br/>
     * PARSE_ALLOW_NUMERIC - indicates tolerance of numeric data when String data may be assumed. eg: YEAR_NAME_FIELD
     * <br/>
     * PRASE_PARTIAL_MATCH - indicates tolerance of partial matches against pattern literals
     * 
     * @internal ICU technology preview
     */
    public enum BooleanAttribute { 
        /** 
         * indicates whitespace tolerance. Also included is trailing dot tolerance. 
         * @internal ICU technology preview
         */
        PARSE_ALLOW_WHITESPACE,
        /** 
         * indicates tolerance of numeric data when String data may be assumed. eg: YEAR_NAME_FIELD 
         * @internal ICU technology preview
         */
        PARSE_ALLOW_NUMERIC, 
        /**
         * indicates tolerance of a partial literal match
         * @draft ICU 53
         */
        PARSE_PARTIAL_MATCH
    };
    
    /**
     * boolean attributes for this instance. Inclusion in this is indicates a true condition.
     */
    private EnumSet<BooleanAttribute> booleanAttributes = EnumSet.allOf(BooleanAttribute.class); 

    // Proclaim serial compatibility with 1.1 FCS
    private static final long serialVersionUID = 7218322306649953788L;

    /**
     * Formats a time object into a time string. Examples of time objects
     * are a time value expressed in milliseconds and a Date object.
     * @param obj must be a Number or a Date or a Calendar.
     * @param toAppendTo the string buffer for the returning time string.
     * @return the formatted time string.
     * @param fieldPosition keeps track of the position of the field
     * within the returned string.
     * On input: an alignment field,
     * if desired. On output: the offsets of the alignment field. For
     * example, given a time text "1996.07.10 AD at 15:08:56 PDT",
     * if the given fieldPosition is DateFormat.YEAR_FIELD, the
     * begin index and end index of fieldPosition will be set to
     * 0 and 4, respectively.
     * Notice that if the same time field appears
     * more than once in a pattern, the fieldPosition will be set for the first
     * occurrence of that time field. For instance, formatting a Date to
     * the time string "1 PM PDT (Pacific Daylight Time)" using the pattern
     * "h a z (zzzz)" and the alignment field DateFormat.TIMEZONE_FIELD,
     * the begin index and end index of fieldPosition will be set to
     * 5 and 8, respectively, for the first occurrence of the timezone
     * pattern character 'z'.
     * @see java.text.Format
     * @stable ICU 2.0
     */
    public final StringBuffer format(Object obj, StringBuffer toAppendTo,
                                     FieldPosition fieldPosition)
    {
        if (obj instanceof Calendar)
            return format( (Calendar)obj, toAppendTo, fieldPosition );
        else if (obj instanceof Date)
            return format( (Date)obj, toAppendTo, fieldPosition );
        else if (obj instanceof Number)
            return format( new Date(((Number)obj).longValue()),
                          toAppendTo, fieldPosition );
        else
            throw new IllegalArgumentException("Cannot format given Object (" +
                                               obj.getClass().getName() + ") as a Date");
    }

    /**
     * Formats a date into a date/time string.
     * @param cal a Calendar set to the date and time to be formatted
     * into a date/time string.  When the calendar type is different from
     * the internal calendar held by this DateFormat instance, the date
     * and the time zone will be inherited from the input calendar, but
     * other calendar field values will be calculated by the internal calendar.
     * @param toAppendTo the string buffer for the returning date/time string.
     * @param fieldPosition keeps track of the position of the field
     * within the returned string.
     * On input: an alignment field,
     * if desired. On output: the offsets of the alignment field. For
     * example, given a time text "1996.07.10 AD at 15:08:56 PDT",
     * if the given fieldPosition is DateFormat.YEAR_FIELD, the
     * begin index and end index of fieldPosition will be set to
     * 0 and 4, respectively.
     * Notice that if the same time field appears
     * more than once in a pattern, the fieldPosition will be set for the first
     * occurrence of that time field. For instance, formatting a Date to
     * the time string "1 PM PDT (Pacific Daylight Time)" using the pattern
     * "h a z (zzzz)" and the alignment field DateFormat.TIMEZONE_FIELD,
     * the begin index and end index of fieldPosition will be set to
     * 5 and 8, respectively, for the first occurrence of the timezone
     * pattern character 'z'.
     * @return the formatted date/time string.
     * @stable ICU 2.0
     */
    public abstract StringBuffer format(Calendar cal, StringBuffer toAppendTo,
                                        FieldPosition fieldPosition);

    /**
     * Formats a Date into a date/time string.
     * @param date a Date to be formatted into a date/time string.
     * @param toAppendTo the string buffer for the returning date/time string.
     * @param fieldPosition keeps track of the position of the field
     * within the returned string.
     * On input: an alignment field,
     * if desired. On output: the offsets of the alignment field. For
     * example, given a time text "1996.07.10 AD at 15:08:56 PDT",
     * if the given fieldPosition is DateFormat.YEAR_FIELD, the
     * begin index and end index of fieldPosition will be set to
     * 0 and 4, respectively.
     * Notice that if the same time field appears
     * more than once in a pattern, the fieldPosition will be set for the first
     * occurrence of that time field. For instance, formatting a Date to
     * the time string "1 PM PDT (Pacific Daylight Time)" using the pattern
     * "h a z (zzzz)" and the alignment field DateFormat.TIMEZONE_FIELD,
     * the begin index and end index of fieldPosition will be set to
     * 5 and 8, respectively, for the first occurrence of the timezone
     * pattern character 'z'.
     * @return the formatted date/time string.
     * @stable ICU 2.0
     */
    public StringBuffer format(Date date, StringBuffer toAppendTo,
                                     FieldPosition fieldPosition) {
        // Use our Calendar object
        calendar.setTime(date);
        return format(calendar, toAppendTo, fieldPosition);
    }

    /**
     * Formats a Date into a date/time string.
     * @param date the time value to be formatted into a time string.
     * @return the formatted time string.
     * @stable ICU 2.0
     */
    public final String format(Date date)
    {
        return format(date, new StringBuffer(64),new FieldPosition(0)).toString();
    }

    /**
     * Parses a date/time string. For example, a time text "07/10/96 4:5 PM, PDT"
     * will be parsed into a Date that is equivalent to Date(837039928046).
     * Parsing begins at the beginning of the string and proceeds as far as
     * possible.  Assuming no parse errors were encountered, this function
     * doesn't return any information about how much of the string was consumed
     * by the parsing.  If you need that information, use a version of
     * parse() that takes a ParsePosition.
     *
     * <p> By default, parsing is lenient: If the input is not in the form used
     * by this object's format method but can still be parsed as a date, then
     * the parse succeeds.  Clients may insist on strict adherence to the
     * format by calling setLenient(false).
     *
     * <p> Note that the normal date formats associated with some calendars - such
     * as the Chinese lunar calendar - do not specify enough fields to enable
     * dates to be parsed unambiguously. In the case of the Chinese lunar
     * calendar, while the year within the current 60-year cycle is specified,
     * the number of such cycles since the start date of the calendar (in the
     * ERA field of the Calendar object) is not normally part of the format,
     * and parsing may assume the wrong era. For cases such as this it is
     * recommended that clients parse using the parse method that takes a Calendar
     * with the Calendar passed in set to the current date, or to a date
     * within the era/cycle that should be assumed if absent in the format.
     *
     * @param text  The date/time string to be parsed
     *
     * @return      A Date, or null if the input could not be parsed
     *
     * @exception  ParseException  If the given string cannot be parsed as a date.
     *
     * @see #parse(String, ParsePosition)
     * @stable ICU 2.0
     */
    public Date parse(String text) throws ParseException
    {
        ParsePosition pos = new ParsePosition(0);
        Date result = parse(text, pos);
        if (pos.getIndex() == 0) // ICU4J
            throw new ParseException("Unparseable date: \"" + text + "\"" ,
                                     pos.getErrorIndex()); // ICU4J
        return result;
    }

    /**
     * Parses a date/time string according to the given parse position.
     * For example, a time text "07/10/96 4:5 PM, PDT" will be parsed
     * into a Calendar that is equivalent to Date(837039928046). Before
     * calling this method the caller should initialize the calendar
     * in one of two ways (unless existing field information is to be kept):
     * (1) clear the calendar, or (2) set the calendar to the current date
     * (or to any date whose fields should be used to supply values that
     * are missing in the parsed date). For example, Chinese calendar dates
     * do not normally provide an era/cycle; in this case the calendar that
     * is passed in should be set to a date within the era that should be
     * assumed, normally the current era.
     *
     * <p> By default, parsing is lenient: If the input is not in the form used
     * by this object's format method but can still be parsed as a date, then
     * the parse succeeds.  Clients may insist on strict adherence to the
     * format by calling setLenient(false).
     *
     * @see #setLenient(boolean)
     *
     * @param text  The date/time string to be parsed
     *
     * @param cal   The calendar set on input to the date and time to be used
     *              for missing values in the date/time string being parsed,
     *              and set on output to the parsed date/time. In general, this
     *              should be initialized before calling this method - either
     *              cleared or set to the current date, depending on desired
     *              behavior. If this parse fails, the calendar may still
     *              have been modified. When the calendar type is different
     *              from the internal calendar held by this DateFormat
     *              instance, calendar field values will be parsed based
     *              on the internal calendar initialized with the time and
     *              the time zone taken from this calendar, then the
     *              parse result (time in milliseconds and time zone) will
     *              be set back to this calendar.
     *
     * @param pos   On input, the position at which to start parsing; on
     *              output, the position at which parsing terminated, or the
     *              start position if the parse failed.
     * @stable ICU 2.0
     */
    public abstract void parse(String text, Calendar cal, ParsePosition pos);

    /**
     * Parses a date/time string according to the given parse position.  For
     * example, a time text "07/10/96 4:5 PM, PDT" will be parsed into a Date
     * that is equivalent to Date(837039928046).
     *
     * <p> By default, parsing is lenient: If the input is not in the form used
     * by this object's format method but can still be parsed as a date, then
     * the parse succeeds.  Clients may insist on strict adherence to the
     * format by calling setLenient(false).
     *
     * <p> Note that the normal date formats associated with some calendars - such
     * as the Chinese lunar calendar - do not specify enough fields to enable
     * dates to be parsed unambiguously. In the case of the Chinese lunar
     * calendar, while the year within the current 60-year cycle is specified,
     * the number of such cycles since the start date of the calendar (in the
     * ERA field of the Calendar object) is not normally part of the format,
     * and parsing may assume the wrong era. For cases such as this it is
     * recommended that clients parse using the parse method that takes a Calendar
     * with the Calendar passed in set to the current date, or to a date
     * within the era/cycle that should be assumed if absent in the format.
     *
     * @see #setLenient(boolean)
     *
     * @param text  The date/time string to be parsed
     *
     * @param pos   On input, the position at which to start parsing; on
     *              output, the position at which parsing terminated, or the
     *              start position if the parse failed.
     *
     * @return      A Date, or null if the input could not be parsed
     * @stable ICU 2.0
     */
    public Date parse(String text, ParsePosition pos) {
        Date result = null;
        int start = pos.getIndex();
        TimeZone tzsav = calendar.getTimeZone();
        calendar.clear();
        parse(text, calendar, pos);
        if (pos.getIndex() != start) {
            try {
                result = calendar.getTime();
            } catch (IllegalArgumentException e) {
                // This occurs if the calendar is non-lenient and there is
                // an out-of-range field.  We don't know which field was
                // illegal so we set the error index to the start.
                pos.setIndex(start);
                pos.setErrorIndex(start);
            }
        }
        // Restore TimeZone
        calendar.setTimeZone(tzsav);
        return result;
    }

    /**
     * Parses a date/time string into an Object.  This convenience method simply
     * calls parse(String, ParsePosition).
     *
     * @see #parse(String, ParsePosition)
     * @stable ICU 2.0
     */
    public Object parseObject (String source, ParsePosition pos)
    {
        return parse(source, pos);
    }

    /**
     * {@icu} Constant for empty style pattern.
     * @stable ICU 3.8
     */
    public static final int NONE = -1;

    /**
     * Constant for full style pattern.
     * @stable ICU 2.0
     */
    public static final int FULL = 0;

    /**
     * Constant for long style pattern.
     * @stable ICU 2.0
     */
    public static final int LONG = 1;

    /**
     * Constant for medium style pattern.
     * @stable ICU 2.0
     */
    public static final int MEDIUM = 2;

    /**
     * Constant for short style pattern.
     * @stable ICU 2.0
     */
    public static final int SHORT = 3;

    /**
     * Constant for default style pattern.  Its value is MEDIUM.
     * @stable ICU 2.0
     */
    public static final int DEFAULT = MEDIUM;

    /**
     * {@icu} Constant for relative style mask.
     * @stable ICU 3.8
     */
    public static final int RELATIVE = (1 << 7);

    /**
     * {@icu} Constant for relative full style pattern.
     * @stable ICU 3.8
     */
    public static final int RELATIVE_FULL = RELATIVE | FULL;

    /**
     * {@icu} Constant for relative style pattern.
     * @stable ICU 3.8
     */
    public static final int RELATIVE_LONG = RELATIVE | LONG;

    /**
     * {@icu} Constant for relative style pattern.
     * @stable ICU 3.8
     */
    public static final int RELATIVE_MEDIUM = RELATIVE | MEDIUM;

    /**
     * {@icu} Constant for relative style pattern.
     * @stable ICU 3.8
     */
    public static final int RELATIVE_SHORT = RELATIVE | SHORT;

    /**
     * {@icu} Constant for relative default style pattern.
     * @stable ICU 3.8
     */
    public static final int RELATIVE_DEFAULT = RELATIVE | DEFAULT;

    /*
     * DATES
     */

    /**
     * {@icu} Constant for date skeleton with year.
     * @stable ICU 4.0
     */
    public static final String YEAR = "y";

    /**
     * {@icu} Constant for date skeleton with quarter.
     * @stable ICU 50
     */
    public static final String QUARTER = "QQQQ";

    /**
     * {@icu} Constant for date skeleton with abbreviated quarter.
     * @stable ICU 50
     */
    public static final String ABBR_QUARTER = "QQQ";

    /**
     * {@icu} Constant for date skeleton with year and quarter.
     * @stable ICU 4.0
     */
    public static final String YEAR_QUARTER = "yQQQQ";

    /**
     * {@icu} Constant for date skeleton with year and abbreviated quarter.
     * @stable ICU 4.0
     */
    public static final String YEAR_ABBR_QUARTER = "yQQQ";

    /**
     * {@icu} Constant for date skeleton with month.
     * @stable ICU 4.0
     */
    public static final String MONTH = "MMMM";

    /**
     * {@icu} Constant for date skeleton with abbreviated month.
     * @stable ICU 4.0
     */
    public static final String ABBR_MONTH = "MMM";

    /**
     * {@icu} Constant for date skeleton with numeric month.
     * @stable ICU 4.0
     */
    public static final String NUM_MONTH = "M";

    /**
     * {@icu} Constant for date skeleton with year and month.
     * @stable ICU 4.0
     */
    public static final String YEAR_MONTH = "yMMMM";

    /**
     * {@icu} Constant for date skeleton with year and abbreviated month.
     * @stable ICU 4.0
     */
    public static final String YEAR_ABBR_MONTH = "yMMM";

    /**
     * {@icu} Constant for date skeleton with year and numeric month.
     * @stable ICU 4.0
     */
    public static final String YEAR_NUM_MONTH = "yM";

    /**
     * {@icu} Constant for date skeleton with day.
     * @stable ICU 4.0
     */
    public static final String DAY = "d";

    /**
     * {@icu} Constant for date skeleton with year, month, and day.
     * Used in combinations date + time, date + time + zone, or time + zone.
     * @stable ICU 4.0
     */
    public static final String YEAR_MONTH_DAY = "yMMMMd";

    /**
     * {@icu} Constant for date skeleton with year, abbreviated month, and day.
     * Used in combinations date + time, date + time + zone, or time + zone.
     * @stable ICU 4.0
     */
    public static final String YEAR_ABBR_MONTH_DAY = "yMMMd";

    /**
     * {@icu} Constant for date skeleton with year, numeric month, and day.
     * Used in combinations date + time, date + time + zone, or time + zone.
     * @stable ICU 4.0
     */
    public static final String YEAR_NUM_MONTH_DAY = "yMd";

    /**
     * {@icu} Constant for date skeleton with weekday.
     * @stable ICU 50
     */
    public static final String WEEKDAY = "EEEE";

    /**
     * {@icu} Constant for date skeleton with abbreviated weekday.
     * @stable ICU 50
     */
    public static final String ABBR_WEEKDAY = "E";

    /**
     * {@icu} Constant for date skeleton with year, month, weekday, and day.
     * Used in combinations date + time, date + time + zone, or time + zone.
     * @stable ICU 4.0
     */
    public static final String YEAR_MONTH_WEEKDAY_DAY = "yMMMMEEEEd";

    /**
     * {@icu} Constant for date skeleton with year, abbreviated month, weekday, and day.
     * Used in combinations date + time, date + time + zone, or time + zone.
     * @stable ICU 4.0
     */
    public static final String YEAR_ABBR_MONTH_WEEKDAY_DAY = "yMMMEd";

    /**
     * {@icu} Constant for date skeleton with year, numeric month, weekday, and day.
     * Used in combinations date + time, date + time + zone, or time + zone.
     * @stable ICU 4.0
     */
    public static final String YEAR_NUM_MONTH_WEEKDAY_DAY = "yMEd";

    /**
     * {@icu} Constant for date skeleton with long month and day.
     * Used in combinations date + time, date + time + zone, or time + zone.
     * @stable ICU 4.0
     */
    public static final String MONTH_DAY = "MMMMd";

    /**
     * {@icu} Constant for date skeleton with abbreviated month and day.
     * Used in combinations date + time, date + time + zone, or time + zone.
     * @stable ICU 4.0
     */
    public static final String ABBR_MONTH_DAY = "MMMd";

    /**
     * {@icu} Constant for date skeleton with numeric month and day.
     * Used in combinations date + time, date + time + zone, or time + zone.
     * @stable ICU 4.0
     */
    public static final String NUM_MONTH_DAY = "Md";

    /**
     * {@icu} Constant for date skeleton with month, weekday, and day.
     * Used in combinations date + time, date + time + zone, or time + zone.
     * @stable ICU 4.0
     */
    public static final String MONTH_WEEKDAY_DAY = "MMMMEEEEd";

    /**
     * {@icu} Constant for date skeleton with abbreviated month, weekday, and day.
     * Used in combinations date + time, date + time + zone, or time + zone.
     * @stable ICU 4.0
     */
    public static final String ABBR_MONTH_WEEKDAY_DAY = "MMMEd";

    /**
     * {@icu} Constant for date skeleton with numeric month, weekday, and day.
     * Used in combinations date + time, date + time + zone, or time + zone.
     * @stable ICU 4.0
     */
    public static final String NUM_MONTH_WEEKDAY_DAY = "MEd";

    /*
     * TIMES
     */

    /**
     * {@icu} Constant for date skeleton with hour, with the locale's preferred hour format (12 or 24).
     * @stable ICU 4.0
     */
    public static final String HOUR = "j";

    /**
     * {@icu} Constant for date skeleton with hour in 24-hour presentation.
     * @stable ICU 50
     */
    public static final String HOUR24 = "H";

    /**
     * {@icu} Constant for date skeleton with minute.
     * @stable ICU 50
     */
    public static final String MINUTE = "m";

    /**
     * {@icu} Constant for date skeleton with hour and minute, with the locale's preferred hour format (12 or 24).
     * Used in combinations date + time, date + time + zone, or time + zone.
     * @stable ICU 4.0
     */
    public static final String HOUR_MINUTE = "jm";

    /**
     * {@icu} Constant for date skeleton with hour and minute in 24-hour presentation.
     * Used in combinations date + time, date + time + zone, or time + zone.
     * @stable ICU 4.0
     */
    public static final String HOUR24_MINUTE = "Hm";

    /**
     * {@icu} Constant for date skeleton with second.
     * @stable ICU 50
     */
    public static final String SECOND = "s";

    /**
     * {@icu} Constant for date skeleton with hour, minute, and second,
     * with the locale's preferred hour format (12 or 24).
     * Used in combinations date + time, date + time + zone, or time + zone.
     * @stable ICU 4.0
     */
    public static final String HOUR_MINUTE_SECOND = "jms";

    /**
     * {@icu} Constant for date skeleton with hour, minute, and second in
     * 24-hour presentation.
     * Used in combinations date + time, date + time + zone, or time + zone.
     * @stable ICU 4.0
     */
    public static final String HOUR24_MINUTE_SECOND = "Hms";

    /**
     * {@icu} Constant for date skeleton with minute and second.
     * Used in combinations date + time, date + time + zone, or time + zone.
     * @stable ICU 4.0
     */
    public static final String MINUTE_SECOND = "ms";

    /*
     * TIMEZONES
     */

    /**
     * {@icu} Constant for <i>generic location format</i>, such as Los Angeles Time;
     * used in combinations date + time + zone, or time + zone.
     * @see <a href="http://unicode.org/reports/tr35/#Date_Format_Patterns">LDML Date Format Patterns</a>
     * @see <a href="http://unicode.org/reports/tr35/#Time_Zone_Fallback">LDML Time Zone Fallback</a>
     * @stable ICU 50
     */
    public static final String LOCATION_TZ = "VVVV";
    
    /**
     * {@icu} Constant for <i>generic non-location format</i>, such as Pacific Time;
     * used in combinations date + time + zone, or time + zone.
     * @see <a href="http://unicode.org/reports/tr35/#Date_Format_Patterns">LDML Date Format Patterns</a>
     * @see <a href="http://unicode.org/reports/tr35/#Time_Zone_Fallback">LDML Time Zone Fallback</a>
     * @stable ICU 50
     */
    public static final String GENERIC_TZ = "vvvv";
    
    /**
     * {@icu} Constant for <i>generic non-location format</i>, abbreviated if possible, such as PT;
     * used in combinations date + time + zone, or time + zone.
     * @see <a href="http://unicode.org/reports/tr35/#Date_Format_Patterns">LDML Date Format Patterns</a>
     * @see <a href="http://unicode.org/reports/tr35/#Time_Zone_Fallback">LDML Time Zone Fallback</a>
     * @stable ICU 50
     */
    public static final String ABBR_GENERIC_TZ = "v";
    
    /**
     * {@icu} Constant for <i>specific non-location format</i>, such as Pacific Daylight Time;
     * used in combinations date + time + zone, or time + zone.
     * @see <a href="http://unicode.org/reports/tr35/#Date_Format_Patterns">LDML Date Format Patterns</a>
     * @see <a href="http://unicode.org/reports/tr35/#Time_Zone_Fallback">LDML Time Zone Fallback</a>
     * @stable ICU 50
     */
    public static final String SPECIFIC_TZ = "zzzz";
    
    /**
     * {@icu} Constant for <i>specific non-location format</i>, abbreviated if possible, such as PDT;
     * used in combinations date + time + zone, or time + zone.
     * @see <a href="http://unicode.org/reports/tr35/#Date_Format_Patterns">LDML Date Format Patterns</a>
     * @see <a href="http://unicode.org/reports/tr35/#Time_Zone_Fallback">LDML Time Zone Fallback</a>
     * @stable ICU 50
     */
    public static final String ABBR_SPECIFIC_TZ = "z";
    
    /**
     * {@icu} Constant for <i>localized GMT/UTC format</i>, such as GMT+8:00 or HPG-8:00;
     * used in combinations date + time + zone, or time + zone.
     * @see <a href="http://unicode.org/reports/tr35/#Date_Format_Patterns">LDML Date Format Patterns</a>
     * @see <a href="http://unicode.org/reports/tr35/#Time_Zone_Fallback">LDML Time Zone Fallback</a>
     * @stable ICU 50
     */
    public static final String ABBR_UTC_TZ = "ZZZZ";

    /*
     * deprecated skeleton constants
     */

    /**
     * {@icu} Constant for date skeleton with standalone month.
     * @deprecated ICU 50 Use {@link #MONTH} instead.
     */
    public static final String STANDALONE_MONTH = "LLLL";

    /**
     * {@icu} Constant for date skeleton with standalone abbreviated month.
     * @deprecated ICU 50 Use {@link #ABBR_MONTH} instead.
     */
    public static final String ABBR_STANDALONE_MONTH = "LLL";

    /**
     * {@icu} Constant for date skeleton with hour, minute, and generic timezone.
     * @deprecated ICU 50 Use instead {@link #HOUR_MINUTE}+{@link #ABBR_GENERIC_TZ} or some other timezone presentation.
     */
    public static final String HOUR_MINUTE_GENERIC_TZ = "jmv";

    /**
     * {@icu} Constant for date skeleton with hour, minute, and timezone.
     * @deprecated ICU 50 Use instead {@link #HOUR_MINUTE}+{@link #ABBR_SPECIFIC_TZ} or some other timezone presentation.
     */
    public static final String HOUR_MINUTE_TZ = "jmz";

    /**
     * {@icu} Constant for date skeleton with hour and generic timezone.
     * @deprecated ICU 50 Use instead {@link #HOUR}+{@link #ABBR_GENERIC_TZ} or some other timezone presentation.
     */
    public static final String HOUR_GENERIC_TZ = "jv";

    /**
     * {@icu} Constant for date skeleton with hour and timezone.
     * @deprecated ICU 50 Use instead {@link #HOUR}+{@link #ABBR_SPECIFIC_TZ} or some other timezone presentation.
     */
    public static final String HOUR_TZ = "jz";


    /**
     * Gets the time formatter with the default formatting style
     * for the default <code>FORMAT</code> locale.
     * @return a time formatter.
     * @see Category#FORMAT
     * @stable ICU 2.0
     */
    public final static DateFormat getTimeInstance()
    {
        return get(-1, DEFAULT, ULocale.getDefault(Category.FORMAT));
    }

    /**
     * Returns the time formatter with the given formatting style
     * for the default <code>FORMAT</code> locale.
     * @param style the given formatting style. For example,
     * SHORT for "h:mm a" in the US locale. Relative time styles are not currently
     * supported, and behave just like the corresponding non-relative style.
     * @return a time formatter.
     * @see Category#FORMAT
     * @stable ICU 2.0
     */
    public final static DateFormat getTimeInstance(int style)
    {
        return get(-1, style, ULocale.getDefault(Category.FORMAT));
    }

    /**
     * Returns the time formatter with the given formatting style
     * for the given locale.
     * @param style the given formatting style. For example,
     * SHORT for "h:mm a" in the US locale. Relative time styles are not currently
     * supported, and behave just like the corresponding non-relative style.
     * @param aLocale the given locale.
     * @return a time formatter.
     * @stable ICU 2.0
     */
    public final static DateFormat getTimeInstance(int style,
                                                 Locale aLocale)
    {
        return get(-1, style, ULocale.forLocale(aLocale));
    }

    /**
     * Returns the time formatter with the given formatting style
     * for the given locale.
     * @param style the given formatting style. For example,
     * SHORT for "h:mm a" in the US locale. Relative time styles are not currently
     * supported, and behave just like the corresponding non-relative style.
     * @param locale the given ulocale.
     * @return a time formatter.
     * @stable ICU 3.2
     */
    public final static DateFormat getTimeInstance(int style,
                                                 ULocale locale)
    {
        return get(-1, style, locale);
    }

    /**
     * Returns the date formatter with the default formatting style
     * for the default <code>FORMAT</code> locale.
     * @return a date formatter.
     * @see Category#FORMAT
     * @stable ICU 2.0
     */
    public final static DateFormat getDateInstance()
    {
        return get(DEFAULT, -1, ULocale.getDefault(Category.FORMAT));
    }

    /**
     * Returns the date formatter with the given formatting style
     * for the default <code>FORMAT</code> locale.
     * @param style the given formatting style. For example,
     * SHORT for "M/d/yy" in the US locale. As currently implemented, relative date
     * formatting only affects a limited range of calendar days before or after the
     * current date, based on the CLDR &lt;field type="day"&gt;/&lt;relative&gt; data: For example,
     * in English, "Yesterday", "Today", and "Tomorrow". Outside of this range, relative
     * dates are formatted using the corresponding non-relative style.
     * @return a date formatter.
     * @see Category#FORMAT
     * @stable ICU 2.0
     */
    public final static DateFormat getDateInstance(int style)
    {
        return get(style, -1, ULocale.getDefault(Category.FORMAT));
    }

    /**
     * Returns the date formatter with the given formatting style
     * for the given locale.
     * @param style the given formatting style. For example,
     * SHORT for "M/d/yy" in the US locale. As currently implemented, relative date
     * formatting only affects a limited range of calendar days before or after the
     * current date, based on the CLDR &lt;field type="day"&gt;/&lt;relative&gt; data: For example,
     * in English, "Yesterday", "Today", and "Tomorrow". Outside of this range, relative
     * dates are formatted using the corresponding non-relative style.
     * @param aLocale the given locale.
     * @return a date formatter.
     * @stable ICU 2.0
     */
    public final static DateFormat getDateInstance(int style,
                                                 Locale aLocale)
    {
        return get(style, -1, ULocale.forLocale(aLocale));
    }

    /**
     * Returns the date formatter with the given formatting style
     * for the given locale.
     * @param style the given formatting style. For example,
     * SHORT for "M/d/yy" in the US locale. As currently implemented, relative date
     * formatting only affects a limited range of calendar days before or after the
     * current date, based on the CLDR &lt;field type="day"&gt;/&lt;relative&gt; data: For example,
     * in English, "Yesterday", "Today", and "Tomorrow". Outside of this range, relative
     * dates are formatted using the corresponding non-relative style.
     * @param locale the given ulocale.
     * @return a date formatter.
     * @stable ICU 3.2
     */
    public final static DateFormat getDateInstance(int style,
                                                 ULocale locale)
    {
        return get(style, -1, locale);
    }

    /**
     * Returns the date/time formatter with the default formatting style
     * for the default <code>FORMAT</code> locale.
     * @return a date/time formatter.
     * @see Category#FORMAT
     * @stable ICU 2.0
     */
    public final static DateFormat getDateTimeInstance()
    {
        return get(DEFAULT, DEFAULT, ULocale.getDefault(Category.FORMAT));
    }

    /**
     * Returns the date/time formatter with the given date and time
     * formatting styles for the default <code>FORMAT</code> locale.
     * @param dateStyle the given date formatting style. For example,
     * SHORT for "M/d/yy" in the US locale. As currently implemented, relative date
     * formatting only affects a limited range of calendar days before or after the
     * current date, based on the CLDR &lt;field type="day"&gt;/&lt;relative&gt; data: For example,
     * in English, "Yesterday", "Today", and "Tomorrow". Outside of this range, relative
     * dates are formatted using the corresponding non-relative style.
     * @param timeStyle the given time formatting style. For example,
     * SHORT for "h:mm a" in the US locale. Relative time styles are not currently
     * supported, and behave just like the corresponding non-relative style.
     * @return a date/time formatter.
     * @see Category#FORMAT
     * @stable ICU 2.0
     */
    public final static DateFormat getDateTimeInstance(int dateStyle,
                                                       int timeStyle)
    {
        return get(dateStyle, timeStyle, ULocale.getDefault(Category.FORMAT));
    }

    /**
     * Returns the date/time formatter with the given formatting styles
     * for the given locale.
     * @param dateStyle the given date formatting style. As currently implemented, relative date
     * formatting only affects a limited range of calendar days before or after the
     * current date, based on the CLDR &lt;field type="day"&gt;/&lt;relative&gt; data: For example,
     * in English, "Yesterday", "Today", and "Tomorrow". Outside of this range, relative
     * dates are formatted using the corresponding non-relative style.
     * @param timeStyle the given time formatting style. Relative time styles are not
     * currently supported, and behave just like the corresponding non-relative style.
     * @param aLocale the given locale.
     * @return a date/time formatter.
     * @stable ICU 2.0
     */
    public final static DateFormat getDateTimeInstance(
        int dateStyle, int timeStyle, Locale aLocale)
    {
        return get(dateStyle, timeStyle, ULocale.forLocale(aLocale));
    }

    /**
     * Returns the date/time formatter with the given formatting styles
     * for the given locale.
     * @param dateStyle the given date formatting style. As currently implemented, relative date
     * formatting only affects a limited range of calendar days before or after the
     * current date, based on the CLDR &lt;field type="day"&gt;/&lt;relative&gt; data: For example,
     * in English, "Yesterday", "Today", and "Tomorrow". Outside of this range, relative
     * dates are formatted using the corresponding non-relative style.
     * @param timeStyle the given time formatting style. Relative time styles are not
     * currently supported, and behave just like the corresponding non-relative style.
     * @param locale the given ulocale.
     * @return a date/time formatter.
     * @stable ICU 3.2
     */
    public final static DateFormat getDateTimeInstance(
        int dateStyle, int timeStyle, ULocale locale)
    {
        return get(dateStyle, timeStyle, locale);
    }

    /**
     * Returns a default date/time formatter that uses the SHORT style for both the
     * date and the time.
     * @stable ICU 2.0
     */
    public final static DateFormat getInstance() {
        return getDateTimeInstance(SHORT, SHORT);
    }

    /**
     * Returns the set of locales for which DateFormats are installed.
     * @return the set of locales for which DateFormats are installed.
     * @stable ICU 2.0
     */
    public static Locale[] getAvailableLocales()
    {
        return ICUResourceBundle.getAvailableLocales();
    }

    /**
     * {@icu} Returns the set of locales for which DateFormats are installed.
     * @return the set of locales for which DateFormats are installed.
     * @draft ICU 3.2 (retain)
     * @provisional This API might change or be removed in a future release.
     */
    public static ULocale[] getAvailableULocales()
    {
        return ICUResourceBundle.getAvailableULocales();
    }

    /**
     * Sets the calendar to be used by this date format.  Initially, the default
     * calendar for the specified or default locale is used.
     * @param newCalendar the new Calendar to be used by the date format
     * @stable ICU 2.0
     */
    public void setCalendar(Calendar newCalendar)
    {
        this.calendar = newCalendar;
    }

    /**
     * Returns the calendar associated with this date/time formatter.
     * @return the calendar associated with this date/time formatter.
     * @stable ICU 2.0
     */
    public Calendar getCalendar()
    {
        return calendar;
    }

    /**
     * Sets the number formatter.
     * @param newNumberFormat the given new NumberFormat.
     * @stable ICU 2.0
     */
    public void setNumberFormat(NumberFormat newNumberFormat)
    {
        this.numberFormat = newNumberFormat;
        /*In order to parse String like "11.10.2001" to DateTime correctly
          in Locale("fr","CH") [Richard/GCL]
        */
        this.numberFormat.setParseIntegerOnly(true);
    }

    /**
     * Returns the number formatter which this date/time formatter uses to
     * format and parse a time.
     * @return the number formatter which this date/time formatter uses.
     * @stable ICU 2.0
     */
    public NumberFormat getNumberFormat()
    {
        return numberFormat;
    }

    /**
     * Sets the time zone for the calendar of this DateFormat object.
     * @param zone the given new time zone.
     * @stable ICU 2.0
     */
    public void setTimeZone(TimeZone zone)
    {
        calendar.setTimeZone(zone);
    }

    /**
     * Returns the time zone.
     * @return the time zone associated with the calendar of DateFormat.
     * @stable ICU 2.0
     */
    public TimeZone getTimeZone()
    {
        return calendar.getTimeZone();
    }

    /**
     * Specifies whether date/time parsing is to be lenient.  With
     * lenient parsing, the parser may use heuristics to interpret inputs that
     * do not precisely match this object's format.  With strict parsing,
     * inputs must match this object's format.
     * <br/><br/> 
     * <b>Note:</b> This method is specific to the encapsulated Calendar object. DateFormat 
     * leniency aspects are controlled by setBooleanAttribute.
     * @param lenient when true, parsing is lenient
     * @see com.ibm.icu.util.Calendar#setLenient
     * @see #setBooleanAttribute(BooleanAttribute, boolean)
     * @stable ICU 2.0
     */
    public void setLenient(boolean lenient)
    {
        calendar.setLenient(lenient);
    }

    /**
     * Returns whether date/time parsing in the encapsulated Calendar object is lenient.
     * @stable ICU 2.0
     */
    public boolean isLenient()
    {
        return calendar.isLenient();
    }

    /** 
     * set a boolean attribute for this instance. Aspects of DateFormat leniency are controlled by
     * boolean attributes. 
     * 
     * @see BooleanAttribute
     * @internal ICU technology preview
     */
    public DateFormat setBooleanAttribute(BooleanAttribute key, boolean value) 
    {
        if(booleanAttributes.contains(key) && value == false)
            booleanAttributes.remove(key);
        
        if(value == true && !booleanAttributes.contains(key))
            booleanAttributes.add(key);
        
        return this;
    }
    
    /**
     * get the current value for the specified BooleanAttribute for this instance
     *
     * if attribute is missing false is returned.
     * 
     * @see BooleanAttribute
     * @internal ICU technology preview
     */
    public boolean getBooleanAttribute(BooleanAttribute key) 
    {
        return booleanAttributes.contains(key);
    }
    
    
    /**
     * Overrides hashCode.
     * @stable ICU 2.0
     */
    ///CLOVER:OFF
    // turn off code coverage since all subclasses override this
    public int hashCode() {
        return numberFormat.hashCode();
        // just enough fields for a reasonable distribution
    }
    ///CLOVER:ON

    /**
     * Overrides equals.
     * @stable ICU 2.0
     */
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DateFormat other = (DateFormat) obj;
        return (calendar.isEquivalentTo(other.calendar) &&
                numberFormat.equals(other.numberFormat));
    }

    /**
     * Overrides clone.
     * @stable ICU 2.0
     */
    public Object clone()
    {
        DateFormat other = (DateFormat) super.clone();
        other.calendar = (Calendar) calendar.clone();
        other.numberFormat = (NumberFormat) numberFormat.clone();
        return other;
    }

    /**
     * Creates a DateFormat with the given time and/or date style in the given
     * locale.
     * @param dateStyle a value from 0 to 3 indicating the time format,
     * or -1 to indicate no date
     * @param timeStyle a value from 0 to 3 indicating the time format,
     * or -1 to indicate no time
     * @param loc the locale for the format
     */
    private static DateFormat get(int dateStyle, int timeStyle, ULocale loc) {
        if((timeStyle != -1 && (timeStyle & RELATIVE)>0) ||
           (dateStyle != -1 && (dateStyle & RELATIVE)>0)) {
            RelativeDateFormat r = new RelativeDateFormat(timeStyle, dateStyle /* offset? */, loc);
            return r;
        }

        if (timeStyle < -1 || timeStyle > 3) {
            throw new IllegalArgumentException("Illegal time style " + timeStyle);
        }
        if (dateStyle < -1 || dateStyle > 3) {
            throw new IllegalArgumentException("Illegal date style " + dateStyle);
        }
        try {
            Calendar cal = Calendar.getInstance(loc);
            DateFormat result = cal.getDateTimeFormat(dateStyle, timeStyle, loc);
            result.setLocale(cal.getLocale(ULocale.VALID_LOCALE),
                 cal.getLocale(ULocale.ACTUAL_LOCALE));
            return result;
        } catch (MissingResourceException e) {
            ///CLOVER:OFF
            // coverage requires separate run with no data, so skip
            return new SimpleDateFormat("M/d/yy h:mm a");
            ///CLOVER:ON
        }
    }

    /**
     * Creates a new date format.
     * @stable ICU 2.0
     */
    protected DateFormat() {}

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    //-------------------------------------------------------------------------
    // Public static interface for creating custon DateFormats for different
    // types of Calendars.
    //-------------------------------------------------------------------------

    /**
     * Creates a {@link DateFormat} object that can be used to format dates in
     * the calendar system specified by <code>cal</code>.
     * <p>
     * @param cal   The calendar system for which a date format is desired.
     *
     * @param dateStyle The type of date format desired.  This can be
     *              {@link DateFormat#SHORT}, {@link DateFormat#MEDIUM},
     *              etc.
     *
     * @param locale The locale for which the date format is desired.
     * @stable ICU 2.0
     */
    static final public DateFormat getDateInstance(Calendar cal, int dateStyle, Locale locale)
    {
        return getDateTimeInstance(cal, dateStyle, -1, ULocale.forLocale(locale));
    }

    /**
     * Creates a {@link DateFormat} object that can be used to format dates in
     * the calendar system specified by <code>cal</code>.
     * <p>
     * @param cal   The calendar system for which a date format is desired.
     *
     * @param dateStyle The type of date format desired.  This can be
     *              {@link DateFormat#SHORT}, {@link DateFormat#MEDIUM},
     *              etc.
     *
     * @param locale The locale for which the date format is desired.
     * @stable ICU 3.2
     */
    static final public DateFormat getDateInstance(Calendar cal, int dateStyle, ULocale locale)
    {
        return getDateTimeInstance(cal, dateStyle, -1, locale);
    }

    /**
     * Creates a {@link DateFormat} object that can be used to format times in
     * the calendar system specified by <code>cal</code>.
     * <p>
     * <b>Note:</b> When this functionality is moved into the core JDK, this method
     * will probably be replaced by a new overload of {@link DateFormat#getInstance}.
     * <p>
     * @param cal   The calendar system for which a time format is desired.
     *
     * @param timeStyle The type of time format desired.  This can be
     *              {@link DateFormat#SHORT}, {@link DateFormat#MEDIUM},
     *              etc.
     *
     * @param locale The locale for which the time format is desired.
     *
     * @see DateFormat#getTimeInstance
     * @stable ICU 2.0
     */
    static final public DateFormat getTimeInstance(Calendar cal, int timeStyle, Locale locale)
    {
        return getDateTimeInstance(cal, -1, timeStyle, ULocale.forLocale(locale));
    }

    /**
     * Creates a {@link DateFormat} object that can be used to format times in
     * the calendar system specified by <code>cal</code>.
     * <p>
     * <b>Note:</b> When this functionality is moved into the core JDK, this method
     * will probably be replaced by a new overload of {@link DateFormat#getInstance}.
     * <p>
     * @param cal   The calendar system for which a time format is desired.
     *
     * @param timeStyle The type of time format desired.  This can be
     *              {@link DateFormat#SHORT}, {@link DateFormat#MEDIUM},
     *              etc.
     *
     * @param locale The locale for which the time format is desired.
     *
     * @see DateFormat#getTimeInstance
     * @stable ICU 3.2
     */
    static final public DateFormat getTimeInstance(Calendar cal, int timeStyle, ULocale locale)
    {
        return getDateTimeInstance(cal, -1, timeStyle, locale);
    }

    /**
     * Creates a {@link DateFormat} object that can be used to format dates and times in
     * the calendar system specified by <code>cal</code>.
     * <p>
     * <b>Note:</b> When this functionality is moved into the core JDK, this method
     * will probably be replaced by a new overload of {@link DateFormat#getInstance}.
     * <p>
     * @param cal   The calendar system for which a date/time format is desired.
     *
     * @param dateStyle The type of date format desired.  This can be
     *              {@link DateFormat#SHORT}, {@link DateFormat#MEDIUM},
     *              etc.
     *
     * @param timeStyle The type of time format desired.  This can be
     *              {@link DateFormat#SHORT}, {@link DateFormat#MEDIUM},
     *              etc.
     *
     * @param locale The locale for which the date/time format is desired.
     *
     * @see DateFormat#getDateTimeInstance
     * @stable ICU 2.0
     */
    static final public DateFormat getDateTimeInstance(Calendar cal, int dateStyle,
                                                 int timeStyle, Locale locale)
    {
        return cal.getDateTimeFormat(dateStyle, timeStyle, ULocale.forLocale(locale));
    }

    /**
     * Creates a {@link DateFormat} object that can be used to format dates and times in
     * the calendar system specified by <code>cal</code>.
     * <p>
     * <b>Note:</b> When this functionality is moved into the core JDK, this method
     * will probably be replaced by a new overload of {@link DateFormat#getInstance}.
     * <p>
     * @param cal   The calendar system for which a date/time format is desired.
     *
     * @param dateStyle The type of date format desired.  This can be
     *              {@link DateFormat#SHORT}, {@link DateFormat#MEDIUM},
     *              etc.
     *
     * @param timeStyle The type of time format desired.  This can be
     *              {@link DateFormat#SHORT}, {@link DateFormat#MEDIUM},
     *              etc.
     *
     * @param locale The locale for which the date/time format is desired.
     *
     * @see DateFormat#getDateTimeInstance
     * @stable ICU 3.2
     */
    static final public DateFormat getDateTimeInstance(Calendar cal, int dateStyle,
                                                 int timeStyle, ULocale locale)
    {
        return cal.getDateTimeFormat(dateStyle, timeStyle, locale);
    }

    /**
     * Returns a date/time formatter that uses the SHORT style
     * for both the date and the time.
     * 
     * @param cal   The calendar system for which a date/time format is desired.
     * @param locale The locale for which the date/time format is desired. 
     * @stable ICU 2.0
     */
    static final public DateFormat getInstance(Calendar cal, Locale locale) {
        return getDateTimeInstance(cal, SHORT, SHORT, ULocale.forLocale(locale));
    }

    /**
     * Returns a date/time formatter that uses the SHORT style
     * for both the date and the time.
     * 
     * @param cal   The calendar system for which a date/time format is desired.
     * @param locale The locale for which the date/time format is desired. 
     * @stable ICU 3.2
     * @provisional This API might change or be removed in a future release.
     */
    static final public DateFormat getInstance(Calendar cal, ULocale locale) {
        return getDateTimeInstance(cal, SHORT, SHORT, locale);
    }

    /**
     * Returns a default date/time formatter that uses the SHORT style for both the
     * date and the time.
     * 
     * @param cal   The calendar system for which a date/time format is desired.
     * @stable ICU 2.0
     */
    static final public DateFormat getInstance(Calendar cal) {
        return getInstance(cal, ULocale.getDefault(Category.FORMAT));
    }

    /**
     * Creates a {@link DateFormat} object for the default locale that can be used
     * to format dates in the calendar system specified by <code>cal</code>.
     * <p>
     * @param cal   The calendar system for which a date format is desired.
     *
     * @param dateStyle The type of date format desired.  This can be
     *              {@link DateFormat#SHORT}, {@link DateFormat#MEDIUM},
     *              etc.
     * @stable ICU 2.0
     */
    static final public DateFormat getDateInstance(Calendar cal, int dateStyle) {
        return getDateInstance(cal, dateStyle, ULocale.getDefault(Category.FORMAT));
    }

    /**
     * Creates a {@link DateFormat} object that can be used to format times in
     * the calendar system specified by <code>cal</code>.
     * <p>
     * <b>Note:</b> When this functionality is moved into the core JDK, this method
     * will probably be replaced by a new overload of {@link DateFormat#getInstance}.
     * <p>
     * @param cal   The calendar system for which a time format is desired.
     *
     * @param timeStyle The type of time format desired.  This can be
     *              {@link DateFormat#SHORT}, {@link DateFormat#MEDIUM},
     *              etc.
     *
     * @see DateFormat#getTimeInstance
     * @stable ICU 2.0
     */
    static final public DateFormat getTimeInstance(Calendar cal, int timeStyle) {
        return getTimeInstance(cal, timeStyle, ULocale.getDefault(Category.FORMAT));
    }

    /**
     * Creates a {@link DateFormat} object for the default locale that can be used to format
     * dates and times in the calendar system specified by <code>cal</code>.
     * <p>
     * <b>Note:</b> When this functionality is moved into the core JDK, this method
     * will probably be replaced by a new overload of {@link DateFormat#getInstance}.
     * <p>
     * @param cal   The calendar system for which a date/time format is desired.
     *
     * @param dateStyle The type of date format desired.  This can be
     *              {@link DateFormat#SHORT}, {@link DateFormat#MEDIUM},
     *              etc.
     *
     * @param timeStyle The type of time format desired.  This can be
     *              {@link DateFormat#SHORT}, {@link DateFormat#MEDIUM},
     *              etc.
     *
     * @see DateFormat#getDateTimeInstance
     * @stable ICU 2.0
     */
    static final public DateFormat getDateTimeInstance(Calendar cal, int dateStyle, int timeStyle) {
        return getDateTimeInstance(cal, dateStyle, timeStyle, ULocale.getDefault(Category.FORMAT));
    }

    /**
     * {@icu} Returns a {@link DateFormat} object that can be used to format dates and times in
     * the default locale.
     *
     * @param skeleton The skeleton that selects the fields to be formatted. (Uses the
     *              {@link DateTimePatternGenerator}.) This can be {@link DateFormat#ABBR_MONTH},
     *              {@link DateFormat#MONTH_WEEKDAY_DAY}, etc.
     *
     * @stable ICU 4.0
     */
    public final static DateFormat getPatternInstance(String skeleton) {
        return getPatternInstance(skeleton, ULocale.getDefault(Category.FORMAT));
    }

    /**
     * {@icu} Returns a {@link DateFormat} object that can be used to format dates and times in
     * the given locale.
     *
     * @param skeleton The skeleton that selects the fields to be formatted. (Uses the
     *              {@link DateTimePatternGenerator}.) This can be {@link DateFormat#ABBR_MONTH},
     *              {@link DateFormat#MONTH_WEEKDAY_DAY}, etc.
     *
     * @param locale The locale for which the date/time format is desired.
     *
     * @stable ICU 4.0
     */
    public final static DateFormat getPatternInstance(String skeleton, Locale locale) {
        return getPatternInstance(skeleton, ULocale.forLocale(locale));
    }

    /**
     * {@icu} Returns a {@link DateFormat} object that can be used to format dates and times in
     * the given locale.
     *
     * @param skeleton The skeleton that selects the fields to be formatted. (Uses the
     *              {@link DateTimePatternGenerator}.) This can be {@link DateFormat#ABBR_MONTH},
     *              {@link DateFormat#MONTH_WEEKDAY_DAY}, etc.
     *
     * @param locale The locale for which the date/time format is desired.
     *
     * @stable ICU 4.0
     */
    public final static DateFormat getPatternInstance(String skeleton, ULocale locale) {
        DateTimePatternGenerator generator = DateTimePatternGenerator.getInstance(locale);
        final String bestPattern = generator.getBestPattern(skeleton);
        return new SimpleDateFormat(bestPattern, locale);
    }

    /**
     * {@icu} Creates a {@link DateFormat} object that can be used to format dates and
     * times in the calendar system specified by <code>cal</code>.
     *
     * @param cal   The calendar system for which a date/time format is desired.
     *
     * @param skeleton The skeleton that selects the fields to be formatted. (Uses the
     *              {@link DateTimePatternGenerator}.)  This can be
     *              {@link DateFormat#ABBR_MONTH}, {@link DateFormat#MONTH_WEEKDAY_DAY},
     *              etc.
     *
     * @param locale The locale for which the date/time format is desired.
     *
     * @stable ICU 4.0
     */
    public final static DateFormat getPatternInstance(Calendar cal, String skeleton, Locale locale) {
        return getPatternInstance(cal, skeleton, ULocale.forLocale(locale));
    }

    /**
     * {@icu} Creates a {@link DateFormat} object that can be used to format dates and
     * times in the calendar system specified by <code>cal</code>.
     *
     * @param cal   The calendar system for which a date/time format is desired.
     *
     * @param skeleton The skeleton that selects the fields to be formatted. (Uses the
     *              {@link DateTimePatternGenerator}.)  This can be
     *              {@link DateFormat#ABBR_MONTH}, {@link DateFormat#MONTH_WEEKDAY_DAY},
     *              etc.
     *
     * @param locale The locale for which the date/time format is desired.
     *
     * @stable ICU 4.0
     */
    public final static DateFormat getPatternInstance(
        Calendar cal, String skeleton, ULocale locale) {
        DateTimePatternGenerator generator = DateTimePatternGenerator.getInstance(locale);
        final String bestPattern = generator.getBestPattern(skeleton);
        SimpleDateFormat format = new SimpleDateFormat(bestPattern, locale);
        format.setCalendar(cal);
        return format;
    }

    /**
     * The instances of this inner class are used as attribute keys and values
     * in AttributedCharacterIterator that
     * DateFormat.formatToCharacterIterator() method returns.
     *
     * <p>There is no public constructor to this class, the only instances are the
     * constants defined here.
     * <p>
     * @stable ICU 3.8
     */
    public static class Field extends Format.Field {

        private static final long serialVersionUID = -3627456821000730829L;

        // Max number of calendar fields
        private static final int CAL_FIELD_COUNT;

        // Table for mapping calendar field number to DateFormat.Field
        private static final Field[] CAL_FIELDS;

        // Map for resolving DateFormat.Field by name
        private static final Map<String, Field> FIELD_NAME_MAP;

        static {
            GregorianCalendar cal = new GregorianCalendar();
            CAL_FIELD_COUNT = cal.getFieldCount();
            CAL_FIELDS = new Field[CAL_FIELD_COUNT];
            FIELD_NAME_MAP = new HashMap<String, Field>(CAL_FIELD_COUNT);
        }

        // Java fields -------------------

        /**
         * Constant identifying the time of day indicator(am/pm).
         * @stable ICU 3.8
         */
        public static final Field AM_PM = new Field("am pm", Calendar.AM_PM);

        /**
         * Constant identifying the day of month field.
         * @stable ICU 3.8
         */
        public static final Field DAY_OF_MONTH = new Field("day of month", Calendar.DAY_OF_MONTH);

        /**
         * Constant identifying the day of week field.
         * @stable ICU 3.8
         */
        public static final Field DAY_OF_WEEK = new Field("day of week", Calendar.DAY_OF_WEEK);

        /**
         * Constant identifying the day of week in month field.
         * @stable ICU 3.8
         */
        public static final Field DAY_OF_WEEK_IN_MONTH =
            new Field("day of week in month", Calendar.DAY_OF_WEEK_IN_MONTH);

        /**
         * Constant identifying the day of year field.
         * @stable ICU 3.8
         */
        public static final Field DAY_OF_YEAR = new Field("day of year", Calendar.DAY_OF_YEAR);

        /**
         * Constant identifying the era field.
         * @stable ICU 3.8
         */
        public static final Field ERA = new Field("era", Calendar.ERA);

        /**
         * Constant identifying the hour(0-23) of day field.
         * @stable ICU 3.8
         */
        public static final Field HOUR_OF_DAY0 = new Field("hour of day", Calendar.HOUR_OF_DAY);

        /**
         * Constant identifying the hour(1-24) of day field.
         * @stable ICU 3.8
         */
        public static final Field HOUR_OF_DAY1 = new Field("hour of day 1", -1);

        /**
         * Constant identifying the hour(0-11) field.
         * @stable ICU 3.8
         */
        public static final Field HOUR0 = new Field("hour", Calendar.HOUR);

        /**
         * Constant identifying the hour(1-12) field.
         * @stable ICU 3.8
         */
        public static final Field HOUR1 = new Field("hour 1", -1);

        /**
         * Constant identifying the millisecond field.
         * @stable ICU 3.8
         */
        public static final Field MILLISECOND = new Field("millisecond", Calendar.MILLISECOND);

        /**
         * Constant identifying the minute field.
         * @stable ICU 3.8
         */
        public static final Field MINUTE = new Field("minute", Calendar.MINUTE);

        /**
         * Constant identifying the month field.
         * @stable ICU 3.8
         */
        public static final Field MONTH = new Field("month", Calendar.MONTH);

        /**
         * Constant identifying the second field.
         * @stable ICU 3.8
         */
        public static final Field SECOND = new Field("second", Calendar.SECOND);

        /**
         * Constant identifying the time zone field.
         * @stable ICU 3.8
         */
        public static final Field TIME_ZONE = new Field("time zone", -1);

        /**
         * Constant identifying the week of month field.
         * @stable ICU 3.8
         */
        public static final Field WEEK_OF_MONTH =
            new Field("week of month", Calendar.WEEK_OF_MONTH);

        /**
         * Constant identifying the week of year field.
         * @stable ICU 3.8
         */
        public static final Field WEEK_OF_YEAR = new Field("week of year", Calendar.WEEK_OF_YEAR);

        /**
         * Constant identifying the year field.
         * @stable ICU 3.8
         */
        public static final Field YEAR = new Field("year", Calendar.YEAR);


        // ICU only fields -------------------

        /**
         * Constant identifying the local day of week field.
         * @stable ICU 3.8
         */
        public static final Field DOW_LOCAL = new Field("local day of week", Calendar.DOW_LOCAL);

        /**
         * Constant identifying the extended year field.
         * @stable ICU 3.8
         */
        public static final Field EXTENDED_YEAR = new Field("extended year", 
                                                            Calendar.EXTENDED_YEAR);

        /**
         * Constant identifying the Julian day field.
         * @stable ICU 3.8
         */
        public static final Field JULIAN_DAY = new Field("Julian day", Calendar.JULIAN_DAY);

        /**
         * Constant identifying the milliseconds in day field.
         * @stable ICU 3.8
         */
        public static final Field MILLISECONDS_IN_DAY =
            new Field("milliseconds in day", Calendar.MILLISECONDS_IN_DAY);

        /**
         * Constant identifying the year used with week of year field.
         * @stable ICU 3.8
         */
        public static final Field YEAR_WOY = new Field("year for week of year", Calendar.YEAR_WOY);

        /**
         * Constant identifying the quarter field.
         * @stable ICU 3.8
         */
        public static final Field QUARTER = new Field("quarter", -1);

        // Stand alone types are variants for its base types.  So we do not define Field for
        // them.
        /*
        public static final Field STANDALONE_DAY =
            new Field("stand alone day of week", Calendar.DAY_OF_WEEK);
        public static final Field STANDALONE_MONTH = new Field("stand alone month", Calendar.MONTH);
        public static final Field STANDALONE_QUARTER = new Field("stand alone quarter", -1);
        */

        // Corresponding calendar field
        private final int calendarField;

        /**
         * Constructs a <code>DateFormat.Field</code> with the given name and
         * the <code>Calendar</code> field which this attribute represents.  Use -1 for
         * <code>calendarField</code> if this field does not have a corresponding
         * <code>Calendar</code> field.
         *
         * @param name          Name of the attribute
         * @param calendarField <code>Calendar</code> field constant
         *
         * @stable ICU 3.8
         */
        protected Field(String name, int calendarField) {
            super(name);
            this.calendarField = calendarField;
            if (this.getClass() == DateFormat.Field.class) {
                FIELD_NAME_MAP.put(name, this);
                if (calendarField >= 0 && calendarField < CAL_FIELD_COUNT) {
                    CAL_FIELDS[calendarField] = this;
                }
            }
        }

        /**
         * Returns the <code>Field</code> constant that corresponds to the <code>
         * Calendar</code> field <code>calendarField</code>.  If there is no
         * corresponding <code>Field</code> is available, null is returned.
         *
         * @param calendarField <code>Calendar</code> field constant
         * @return <code>Field</code> associated with the <code>calendarField</code>,
         * or null if no associated <code>Field</code> is available.
         * @throws IllegalArgumentException if <code>calendarField</code> is not
         * a valid <code>Calendar</code> field constant.
         *
         * @stable ICU 3.8
         */
        public static DateFormat.Field ofCalendarField(int calendarField) {
            if (calendarField < 0 || calendarField >= CAL_FIELD_COUNT) {
                throw new IllegalArgumentException("Calendar field number is out of range");
            }
            return CAL_FIELDS[calendarField];
        }

        /**
         * Returns the <code>Calendar</code> field associated with this attribute.
         * If there is no corresponding <code>Calendar</code> available, this will
         * return -1.
         *
         * @return <code>Calendar</code> constant for this attribute.
         *
         * @stable ICU 3.8
         */
        public int getCalendarField() {
            return calendarField;
        }

        /**
         * Resolves instances being deserialized to the predefined constants.
         *
         * @throws InvalidObjectException if the constant could not be resolved.
         *
         * @stable ICU 3.8
         */
        protected Object readResolve() throws InvalidObjectException {
            ///CLOVER:OFF
            if (this.getClass() != DateFormat.Field.class) {
                throw new InvalidObjectException(
                    "A subclass of DateFormat.Field must implement readResolve.");
            }
            ///CLOVER:ON
            Object o = FIELD_NAME_MAP.get(this.getName());
            ///CLOVER:OFF
            if (o == null) {
                throw new InvalidObjectException("Unknown attribute name.");
            }
            ///CLOVER:ON
            return o;
        }
    }
}
