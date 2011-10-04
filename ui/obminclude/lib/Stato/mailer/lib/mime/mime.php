<?php

class SMimeException extends Exception {}

/**
 * Tooling class for MultiPart Mime Messages
 *
 * @package    Stato
 * @subpackage mailer
 */
class SMime
{
    const QUOTED_PRINTABLE = 'quoted-printable';
    
    const BASE64 = 'base64';
    
    const LINE_LENGTH = 72;
    
    const EOL = "\n";
    
    private static $unprintable_chars = "\x00\x01\x02\x03\x04\x05\x06\x07\x08\x09\x0A\x0B\x0C\x0D\x0E\x0F\x10\x11\x12\x13\x14\x15\x16\x17\x18\x19\x1A\x1B\x1C\x1D\x1E\x1F\x7F\x80\x81\x82\x83\x84\x85\x86\x87\x88\x89\x8A\x8B\x8C\x8D\x8E\x8F\x90\x91\x92\x93\x94\x95\x96\x97\x98\x99\x9A\x9B\x9C\x9D\x9E\x9F\xA0\xA1\xA2\xA3\xA4\xA5\xA6\xA7\xA8\xA9\xAA\xAB\xAC\xAD\xAE\xAF\xB0\xB1\xB2\xB3\xB4\xB5\xB6\xB7\xB8\xB9\xBA\xBB\xBC\xBD\xBE\xBF\xC0\xC1\xC2\xC3\xC4\xC5\xC6\xC7\xC8\xC9\xCA\xCB\xCC\xCD\xCE\xCF\xD0\xD1\xD2\xD3\xD4\xD5\xD6\xD7\xD8\xD9\xDA\xDB\xDC\xDD\xDE\xDF\xE0\xE1\xE2\xE3\xE4\xE5\xE6\xE7\xE8\xE9\xEA\xEB\xEC\xED\xEE\xEF\xF0\xF1\xF2\xF3\xF4\xF5\xF6\xF7\xF8\xF9\xFA\xFB\xFC\xFD\xFE\xFF";

    /**
     * Checks if the given string contains no unprintable characters
     *
     * @param string $str
     * @return boolean
     */
    public static function is_printable($str)
    {
        return (strcspn($str, self::$unprintable_chars) == strlen($str));
    }
    
    /**
     * Encodes the given string or stream with the given encoding
     *
     * @param string $content
     * @param string $encoding
     * @param int $LineLength; defaults to SMime::LINE_LENGTH (72)
     * @param string $eol EOL string; defaults to SMime::EOL (\n)
     * @return string
     */
    public static function encode($content, $encoding, $line_length = self::LINE_LENGTH, $eol = self::EOL)
    {
        if (!self::is_encoding_supported($encoding))
            throw new SMimeException("Not supported encoding: $encoding");
        
        if (is_resource($content))
            return self::encode_stream($content, $encoding, $line_length, $eol);
            
        switch ($encoding) {
            case self::BASE64:
                return self::encode_base64($content, $line_length, $eol);
            case self::QUOTED_PRINTABLE:
                return self::encode_quoted_printable($content, $line_length, $eol);
            default:
                return $content;
        }
    }
    
    /**
     * Encodes the given stream with the given encoding using stream filters
     * 
     * If the given encoding stream filter is not available, a SMimeException
     * will be thrown.
     *
     * @param string $stream
     * @param string $encoding
     * @param int $LineLength; defaults to SMime::LINE_LENGTH (72)
     * @param string $eol EOL string; defaults to SMime::EOL (\n)
     * @return string
     */
    public static function encode_stream($stream, $encoding, $line_length = self::LINE_LENGTH, $eol = self::EOL)
    {
        if (!self::is_encoding_supported($encoding))
            throw new SMimeException("Not supported encoding: $encoding");
        
        switch ($encoding) {
            case self::BASE64:
                $filter = 'convert.base64-encode';
                break;
            case self::QUOTED_PRINTABLE:
                $filter = 'convert.quoted-printable-encode';
                break;
            default:
                $filter = null;
        }
        if ($filter !== null) {
            $params = array('line-length' => $line_length, 
                            'line-break-chars' => $eol);
            $stream_filter = stream_filter_append($stream, $filter, STREAM_FILTER_READ, $params);
        }
        $content = stream_get_contents($stream);
        if ($filter !== null) stream_filter_remove($stream_filter);
        fclose($stream);
        return $content;
    }
    
    /**
     * Encodes a given string in base64 encoding and break lines
     * according to the given maximum linelength
     *
     * @param string $string
     * @param int $LineLength; defaults to SMime::LINE_LENGTH (72)
     * @param string $eol EOL string; defaults to SMime::EOL (\n)
     * @return string
     */
    public static function encode_base64($string, $line_length = self::LINE_LENGTH, $eol = self::EOL)
    {
        return rtrim(chunk_split(base64_encode($string), $line_length, $eol));
    }
    
    /**
     * Encodes a given string with the QUOTED_PRINTABLE mechanism and break lines
     * according to the given maximum linelength
     *
     * @param string $string
     * @param int $LineLength; defaults to SMime::LINE_LENGTH (72)
     * @param string $eol EOL string; defaults to SMime::EOL (\n)
     * @return string
     */
    public static function encode_quoted_printable($string, $line_length = self::LINE_LENGTH, $eol = self::EOL)
    {
        // this works, but replaces more characters than the minimum set.
        return preg_replace('/[^\r\n]{73}[^=\r\n]{2}/', "$0=\r\n",
               str_replace("%", "=", str_replace("%0D%0A", "\r\n",
               str_replace("%20"," ",rawurlencode($string)))));
    }
    
    /**
     * Checks if the given encoding is supported
     *
     * @param string $encoding
     * @return boolean
     */
    private static function is_encoding_supported($encoding)
    {
        return in_array($encoding, array(self::BASE64, self::QUOTED_PRINTABLE, '7bit', '8bit'));
    }
}