package com.github.niradler.checkovjetbrainsidea.services.checkov

import java.nio.file.Paths

class PipCheckovRunner: CheckovRunner {
    private var checkovPath: String? = null

    private fun isCheckovInstalledGlobally(): Boolean {
        try {
            Runtime.getRuntime().exec("checkov -v")
            return true
        } catch (err: Exception) {
            return false
        }
    }

    private fun getPythonUserBasePath(): String {
        val execResult = Runtime.getRuntime().exec("python3 -c \'import site; print(site.USER_BASE)\'")
        val pythonUserBase = execResult.outputStream.toString().trim()
        return Paths.get(pythonUserBase, "bin", "checkov").toString()
    }

    override fun installOrUpdate(): Boolean {
        try {
            println("Trying to install Checkov using pip3.")
            Runtime.getRuntime().exec("pip3 install -U --user --verbose checkov -i https://pypi.org/simple/")
            println("Checkov installed with pip3 successfully.")

            if (isCheckovInstalledGlobally()) {
                this.checkovPath = "checkov"
            } else {
                this.checkovPath = this.getPythonUserBasePath()
            }

            return true
        } catch (err :Exception) {
            println("Failed to install Checkov using pip3.")
            err.printStackTrace()
            return false
        }
    }

    override fun run() {
        TODO("Not yet implemented")
    }
}

//const installOrUpdateCheckovWithPip3 = async (logger: Logger): Promise<string | null> => {
//    try {
//        logger.info('Trying to install Checkov using pip3.');
//        await asyncExec('pip3 install -U --user --verbose checkov -i https://pypi.org/simple/');
//
//        if (await isCheckovInstalledGlobally()) {
//            const checkovPath = 'checkov';
//            logger.info('Checkov installed successfully using pip3.', { checkovPath });
//            return checkovPath;
//        }
//
//        const [pythonUserBaseOutput] = await asyncExec('python3 -c \'import site; print(site.USER_BASE)\'');
//        const checkovPath = path.join(pythonUserBaseOutput.trim(), 'bin', 'checkov');
//        return checkovPath;
//    } catch (error) {
//        logger.error('Failed to install or update Checkov using pip3. Error:', { error });
//        return null;
//    }
//};