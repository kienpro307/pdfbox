package org.apache.pdfbox;

import org.apache.pdfbox.io.io2.*;
import org.apache.pdfbox.io.io2.RandomAccessStreamCache.StreamCacheCreateFunction;
import org.apache.pdfbox.pdfparser.FDFParser;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.fdf.FDFDocument;
import org.apache.pdfbox.util.XMLUtil;

import java.io.*;

public class Loader
{

    private Loader()
    {
    }

    /**
     * This will load a document from a file.
     *
     * @param filename The name of the file to load. {@link org.apache.pdfbox.io.io2.RandomAccessReadBufferedFile} is used
     * to read the file.
     *
     * @return The document that was loaded.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    public static FDFDocument loadFDF(String filename) throws IOException
    {
        return Loader.loadFDF(new File(filename));
    }

    /**
     * This will load a document from a file.
     *
     * @param file The name of the file to load. {@link org.apache.pdfbox.io.io2.RandomAccessReadBufferedFile} is used to
     * read the file.
     *
     * @return The document that was loaded.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    public static FDFDocument loadFDF(File file) throws IOException
    {
        RandomAccessRead readBuffer = new RandomAccessReadBufferedFile(file);
        try
        {
            FDFParser parser = new FDFParser(readBuffer);
            return parser.parse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This will load a document from an input stream. The stream is loaded to the memory to establish random access to
     * the data.
     *
     * @param input The stream that contains the document. To read the stream
     * {@link org.apache.pdfbox.io.io2.RandomAccessReadBuffer} is used
     *
     * @return The document that was loaded.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    public static FDFDocument loadFDF(InputStream input) throws IOException
    {
        RandomAccessRead readBuffer = new RandomAccessReadBuffer(input);
        try
        {
            FDFParser parser = new FDFParser(readBuffer);
            return parser.parse();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This will load a document from a file.
     *
     * @param filename The name of the file to load.
     *
     * @return The document that was loaded.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    public static FDFDocument loadXFDF(String filename) throws IOException
    {
        return Loader.loadXFDF(new BufferedInputStream(new FileInputStream(filename)));
    }

    /**
     * This will load a document from a file.
     *
     * @param file The name of the file to load.
     *
     * @return The document that was loaded.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    public static FDFDocument loadXFDF(File file) throws IOException
    {
        return Loader.loadXFDF(new BufferedInputStream(new FileInputStream(file)));
    }

    /**
     * This will load a document from an input stream. The stream is loaded to the memory to establish random access to
     * the data.
     *
     * @param input The stream that contains the document.
     *
     * @return The document that was loaded.
     *
     * @throws IOException If there is an error reading from the stream.
     */
    public static FDFDocument loadXFDF(InputStream input) throws IOException
    {
        return new FDFDocument(XMLUtil.parse(input));
    }

    /**
     * Parses a PDF. Unrestricted main memory will be used for buffering PDF streams.
     *
     * @param input byte array that contains the document. {@link org.apache.pdfbox.io.io2.RandomAccessReadBuffer} is used
     * to read the data.
     *
     * @return loaded document
     *
     * @throws InvalidPasswordException If the PDF required a non-empty password.
     * @throws IOException In case of a reading or parsing error.
     */
    public static PDDocument loadPDF(byte[] input) throws IOException
    {
        return Loader.loadPDF(input, "");
    }

    /**
     * Parses a PDF. Unrestricted main memory will be used for buffering PDF streams.
     *
     * @param input byte array that contains the document. {@link org.apache.pdfbox.io.io2.RandomAccessReadBuffer} is used
     * to read the data.
     * @param password password to be used for decryption
     *
     * @return loaded document
     *
     * @throws InvalidPasswordException If the password is incorrect.
     * @throws IOException In case of a reading or parsing error.
     */
    public static PDDocument loadPDF(byte[] input, String password) throws IOException
    {
        return Loader.loadPDF(input, password, null, null);
    }

    /**
     * Parses a PDF. Unrestricted main memory will be used for buffering PDF streams.
     *
     * @param input byte array that contains the document. {@link org.apache.pdfbox.io.io2.RandomAccessReadBuffer} is used
     * to read the data.
     * @param password password to be used for decryption
     * @param keyStore key store to be used for decryption when using public key security
     * @param alias alias to be used for decryption when using public key security
     *
     * @return loaded document
     *
     * @throws InvalidPasswordException If the password is incorrect.
     * @throws IOException In case of a reading or parsing error.
     */
    public static PDDocument loadPDF(byte[] input, String password, InputStream keyStore, String alias)
            throws IOException
    {
        return Loader.loadPDF(input, password, keyStore, alias, IOUtils.createMemoryOnlyStreamCache());
    }

    /**
     * Parses a PDF.
     *
     * @param input byte array that contains the document. {@link org.apache.pdfbox.io.io2.RandomAccessReadBuffer} is used
     * to read the data.
     * @param password password to be used for decryption
     * @param keyStore key store to be used for decryption when using public key security
     * @param alias alias to be used for decryption when using public key security
     * @param streamCacheCreateFunction a function to create an instance of a stream cache to be used for buffering
     * new/altered PDF streams
     *
     * @return loaded document
     *
     * @throws InvalidPasswordException If the password is incorrect.
     * @throws IOException In case of a reading or parsing error.
     */
    public static PDDocument loadPDF(byte[] input, String password, InputStream keyStore, String alias,
                                     StreamCacheCreateFunction streamCacheCreateFunction) throws IOException
    {
        RandomAccessRead source = null;
        try
        {
            // RandomAccessRead is not closed here, may be needed for signing
            source = new RandomAccessReadBuffer(input);
            PDFParser parser = new PDFParser(source, password, keyStore, alias, streamCacheCreateFunction);
            return parser.parse();
        }
        catch (IOException ioe)
        {
            IOUtils.closeQuietly(source);
            throw ioe;
        }
    }

    /**
     * Parses a PDF. Unrestricted main memory will be used for buffering PDF streams.
     *
     * @param file file to be loaded. {@link org.apache.pdfbox.io.io2.RandomAccessReadBufferedFile} is used to read the
     * file.
     *
     * @return loaded document
     *
     * @throws InvalidPasswordException If the file required a non-empty password.
     * @throws IOException in case of a file reading or parsing error
     */
    public static PDDocument loadPDF(File file) throws IOException
    {
        return Loader.loadPDF(file, "", IOUtils.createMemoryOnlyStreamCache());
    }

    /**
     * Parses a PDF.
     *
     * @param file file to be loaded. {@link org.apache.pdfbox.io.io2.RandomAccessReadBufferedFile} is used to read the
     * file.
     * @param streamCacheCreateFunction a function to create an instance of a stream cache to be used for buffering
     * new/altered PDF streams
     *
     * @return loaded document
     *
     * @throws InvalidPasswordException If the file required a non-empty password.
     * @throws IOException in case of a file reading or parsing error
     */
    public static PDDocument loadPDF(File file, StreamCacheCreateFunction streamCacheCreateFunction)
            throws IOException
    {
        return Loader.loadPDF(file, "", null, null, streamCacheCreateFunction);
    }

    /**
     * Parses a PDF. Unrestricted main memory will be used for buffering PDF streams.
     *
     * @param file file to be loaded. {@link org.apache.pdfbox.io.io2.RandomAccessReadBufferedFile} is used to read the
     * file.
     * @param password password to be used for decryption
     *
     * @return loaded document
     *
     * @throws InvalidPasswordException If the password is incorrect.
     * @throws IOException in case of a file reading or parsing error
     */
    public static PDDocument loadPDF(File file, String password) throws IOException
    {
        return Loader.loadPDF(file, password, null, null, IOUtils.createMemoryOnlyStreamCache());
    }

    /**
     * Parses a PDF.
     *
     * @param file file to be loaded. {@link org.apache.pdfbox.io.io2.RandomAccessReadBufferedFile} is used to read the
     * file.
     * @param password password to be used for decryption
     * @param streamCacheCreateFunction a function to create an instance of a stream cache to be used for buffering
     * new/altered PDF streams
     *
     * @return loaded document
     *
     * @throws InvalidPasswordException If the password is incorrect.
     * @throws IOException in case of a file reading or parsing error
     */
    public static PDDocument loadPDF(File file, String password,
                                     StreamCacheCreateFunction streamCacheCreateFunction)
            throws IOException
    {
        return Loader.loadPDF(file, password, null, null, streamCacheCreateFunction);
    }

    /**
     * Parses a PDF. Unrestricted main memory will be used for buffering PDF streams.
     *
     * @param file file to be loaded. {@link org.apache.pdfbox.io.io2.RandomAccessReadBufferedFile} is used to read the
     * file.
     * @param password password to be used for decryption
     * @param keyStore key store to be used for decryption when using public key security
     * @param alias alias to be used for decryption when using public key security
     *
     * @return loaded document
     *
     * @throws IOException in case of a file reading or parsing error
     */
    public static PDDocument loadPDF(File file, String password, InputStream keyStore, String alias)
            throws IOException
    {
        return Loader.loadPDF(file, password, keyStore, alias, IOUtils.createMemoryOnlyStreamCache());
    }

    /**
     * Parses a PDF.
     *
     * @param file file to be loaded. {@link org.apache.pdfbox.io.io2.RandomAccessReadBufferedFile} is used to read the
     * file.
     * @param password password to be used for decryption
     * @param keyStore key store to be used for decryption when using public key security
     * @param alias alias to be used for decryption when using public key security
     * @param streamCacheCreateFunction a function to create an instance of a stream cache to be used for buffering
     * new/altered PDF streams
     *
     * @return loaded document
     *
     * @throws IOException in case of a file reading or parsing error
     */
    public static PDDocument loadPDF(File file, String password, InputStream keyStore, String alias,
                                     StreamCacheCreateFunction streamCacheCreateFunction) throws IOException
    {
        RandomAccessRead raFile = null;
        try
        {
            // RandomAccessRead is not closed here, may be needed for signing
            raFile = new RandomAccessReadBufferedFile(file);
            return Loader.loadPDF(raFile, password, keyStore, alias, streamCacheCreateFunction);
        }
        catch (IOException ioe)
        {
            IOUtils.closeQuietly(raFile);
            throw ioe;
        }
    }

    /**
     * Parses a PDF. Unrestricted main memory will be used for buffering PDF new streams.
     *
     * @param randomAccessRead random access read representing the pdf to be loaded
     *
     * @return loaded document
     *
     * @throws InvalidPasswordException If the PDF required a non-empty password.
     * @throws IOException In case of a reading or parsing error.
     */
    public static PDDocument loadPDF(RandomAccessRead randomAccessRead) throws IOException
    {
        return Loader.loadPDF(randomAccessRead, "", null, null, IOUtils.createMemoryOnlyStreamCache());
    }

    /**
     * Parses a PDF.
     *
     * @param randomAccessRead random access read representing the pdf to be loaded
     * @param streamCacheCreateFunction a function to create an instance of a stream cache to be used for buffering
     * new/altered PDF streams
     *
     * @return loaded document
     *
     * @throws InvalidPasswordException If the PDF required a non-empty password.
     * @throws IOException In case of a reading or parsing error.
     */
    public static PDDocument loadPDF(RandomAccessRead randomAccessRead,
                                     RandomAccessStreamCache.StreamCacheCreateFunction streamCacheCreateFunction)
            throws IOException
    {
        return Loader.loadPDF(randomAccessRead, "", null, null, streamCacheCreateFunction);
    }

    /**
     * Parses a PDF. Unrestricted main memory will be used for buffering new/altered PDF streams.
     *
     * @param randomAccessRead random access read representing the pdf to be loaded
     * @param password password to be used for decryption
     *
     * @return loaded document
     *
     * @throws InvalidPasswordException If the password is incorrect.
     * @throws IOException In case of a reading or parsing error.
     */
    public static PDDocument loadPDF(RandomAccessRead randomAccessRead, String password) throws IOException
    {
        return Loader.loadPDF(randomAccessRead, password, null, null,
                IOUtils.createMemoryOnlyStreamCache());
    }

    /**
     * Parses a PDF. Unrestricted main memory will be used for buffering new/altered PDF streams.
     *
     * @param randomAccessRead random access read representing the pdf to be loaded
     * @param password password to be used for decryption
     * @param keyStore key store to be used for decryption when using public key security
     * @param alias alias to be used for decryption when using public key security
     *
     * @return loaded document
     *
     * @throws IOException In case of a reading or parsing error.
     */
    public static PDDocument loadPDF(RandomAccessRead randomAccessRead, String password, InputStream keyStore,
                                     String alias) throws IOException
    {
        return Loader.loadPDF(randomAccessRead, password, keyStore, alias,
                IOUtils.createMemoryOnlyStreamCache());
    }

    /**
     * Parses a PDF.
     *
     * @param randomAccessRead random access read representing the pdf to be loaded
     * @param password password to be used for decryption
     * @param streamCacheCreateFunction a function to create an instance of a stream cache to be used for buffering
     * new/altered PDF streams
     *
     * @return loaded document
     *
     * @throws InvalidPasswordException If the password is incorrect.
     * @throws IOException In case of a reading or parsing error.
     */
    public static PDDocument loadPDF(RandomAccessRead randomAccessRead, String password,
                                     RandomAccessStreamCache.StreamCacheCreateFunction streamCacheCreateFunction) throws IOException
    {
        return Loader.loadPDF(randomAccessRead, password, null, null, streamCacheCreateFunction);
    }

    /**
     * Parses a PDF.
     *
     * @param randomAccessRead random access read representing the pdf to be loaded
     * @param password password to be used for decryption
     * @param keyStore key store to be used for decryption when using public key security
     * @param alias alias to be used for decryption when using public key security
     * @param streamCacheCreateFunction a function to create an instance of a stream cache to be used for buffering
     * new/altered PDF streams
     *
     * @return loaded document
     *
     * @throws IOException in case of a file reading or parsing error
     */
    public static PDDocument loadPDF(RandomAccessRead randomAccessRead, String password,
                                     InputStream keyStore, String alias, RandomAccessStreamCache.StreamCacheCreateFunction streamCacheCreateFunction)
            throws IOException
    {
        PDFParser parser = new PDFParser(randomAccessRead, password, keyStore, alias,
                streamCacheCreateFunction);
        return parser.parse();
    }

}
