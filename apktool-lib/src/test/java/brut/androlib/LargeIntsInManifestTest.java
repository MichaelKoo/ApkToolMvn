/**
 *  Copyright 2014 Ryszard Wiśniewski <brut.alll@gmail.com>
 *  Copyright 2014 Connor Tumbleson <connor.tumbleson@gmail.com>
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
import java.io.*;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.custommonkey.xmlunit.*;
import org.junit.*;
import static org.junit.Assert.*;
import org.xml.sax.SAXException;

public class LargeIntsInManifestTest {

    @BeforeClass
    public static void beforeClass() throws Exception, BrutException {
        TestUtils.cleanFrameworkFile();
        sTmpDir = new ExtFile(OS.createTempDirectory());
        TestUtils.copyResourceDir(LargeIntsInManifestTest.class, "brut/apktool/issue767/", sTmpDir);
    }

    @AfterClass
    public static void afterClass() throws BrutException {
        OS.rmdir(sTmpDir);
    }

    @Test
    public void checkIfLargeIntsAreHandledTest() throws BrutException, IOException {
        String apk = "issue767.apk";

        // decode issue767.apk
        ApkDecoder apkDecoder = new ApkDecoder(new File(sTmpDir + File.separator + apk));
        sTestOrigDir = new ExtFile(sTmpDir + File.separator + apk + ".out");

        apkDecoder.setOutDir(new File(sTmpDir + File.separator + apk + ".out"));
        apkDecoder.decode();

        // build issue767
        ExtFile testApk = new ExtFile(sTmpDir, apk + ".out");
        new Androlib().build(testApk, null);
        String newApk = apk + ".out" + File.separator + "dist" + File.separator + apk;

        // decode issue767 again
        apkDecoder = new ApkDecoder(new File(sTmpDir + File.separator + newApk));
        sTestNewDir = new ExtFile(sTmpDir + File.separator + apk + ".out.two");

        apkDecoder.setOutDir(new File(sTmpDir + File.separator + apk + ".out.two"));
        apkDecoder.decode();

        compareXmlFiles("AndroidManifest.xml");
    }

    private void compareXmlFiles(String path) throws BrutException {
        compareXmlFiles(path, null);
    }

    private void compareXmlFiles(String path, ElementQualifier qualifier) throws BrutException {
        DetailedDiff diff;
        try {
            Reader control = new FileReader(new File(sTestOrigDir, path));
            Reader test = new FileReader(new File(sTestNewDir, path));

            diff = new DetailedDiff(new Diff(control, test));
        } catch (SAXException | IOException ex) {
            throw new BrutException(ex);
        }

        if (qualifier != null) {
            diff.overrideElementQualifier(qualifier);
        }

        assertTrue(path + ": " + diff.getAllDifferences().toString(), diff.similar());
    }

    private static ExtFile sTmpDir;
    private static ExtFile sTestOrigDir;
    private static ExtFile sTestNewDir;

    private final static Logger LOGGER = Logger.getLogger(BuildAndDecodeTest.class.getName());
}
