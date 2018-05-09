/**
 *  Copyright 2014 Ryszard Wiśniewski <brut.alll@gmail.com>
 *  Copyright 2016 Connor Tumbleson <connor.tumbleson@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package brut.androlib;

import brut.directory.ExtFile;
import brut.common.BrutException;
import brut.util.OS;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

/**
 * @author Connor Tumbleson <connor.tumbleson@gmail.com>
 */
public class UnknownCompressionTest {

    @BeforeClass
    public static void beforeClass() throws Exception, BrutException {
        TestUtils.cleanFrameworkFile();
        sTmpDir = new ExtFile(OS.createTempDirectory());
        TestUtils.copyResourceDir(LargeIntsInManifestTest.class, "brut/apktool/unknown_compression/", sTmpDir);

        String apk = "deflated_unknowns.apk";
        ApkOptions apkOptions = new ApkOptions();
        apkOptions.frameworkFolderLocation = sTmpDir.getAbsolutePath();

        sOriginalFile = new ExtFile(sTmpDir, apk);

        // decode deflated_unknowns.apk
        ApkDecoder apkDecoder = new ApkDecoder(sOriginalFile);
        apkDecoder.setOutDir(new File(sOriginalFile.getAbsolutePath() + ".out"));
        apkDecoder.decode();

        // build deflated_unknowns
        ExtFile clientApkFolder = new ExtFile(sOriginalFile.getAbsolutePath() + ".out");
        new Androlib(apkOptions).build(clientApkFolder, null);
        sBuiltFile = new ExtFile(clientApkFolder, "dist" + File.separator + apk);

    }

    @AfterClass
    public static void afterClass() throws BrutException {
        OS.rmdir(sTmpDir);
    }

    @Test
    public void pkmExtensionDeflatedTest() throws BrutException, IOException {
        Integer control = sOriginalFile.getDirectory().getCompressionLevel("assets/bin/Data/test.pkm");
        Integer rebuilt = sBuiltFile.getDirectory().getCompressionLevel("assets/bin/Data/test.pkm");

        // Check that control = rebuilt (both deflated)
        // Add extra check for checking not equal to 0, just in case control gets broken
        assertEquals(control, rebuilt);
        assertNotSame(0, rebuilt);
    }

    @Test
    public void doubleExtensionStoredTest() throws BrutException, IOException {
        Integer control = sOriginalFile.getDirectory().getCompressionLevel("assets/bin/Data/two.extension.file");
        Integer rebuilt = sBuiltFile.getDirectory().getCompressionLevel("assets/bin/Data/two.extension.file");

        // Check that control = rebuilt (both stored)
        // Add extra check for checking = 0 to enforce check for stored just in case control breaks
        assertEquals(control, rebuilt);
        assertEquals(new Integer(0), rebuilt);
    }

    private static ExtFile sTmpDir;

    private static ExtFile sOriginalFile;
    private static ExtFile sBuiltFile;

    private final static Logger LOGGER = Logger.getLogger(BuildAndDecodeJarTest.class.getName());
}