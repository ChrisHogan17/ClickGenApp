package edu.washington.hoganc17.clickgen

import com.musicg.wave.Wave
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class AudioManager {

    fun mixAmplitudesSixteenBit(trackOne: ShortArray, trackTwo: ShortArray): ByteArray {
        // Our Code
        // 16 bit + 16 bit = 16 bit file

        // Since each value in the sampleAmplitudes is 2 bytes
        // Make output byte array the length of the longest array * 2
        val output = ByteArray(if (trackOne.size > trackTwo.size) trackTwo.size * 2 else trackOne.size * 2)

        var outputIndex = 0
        for (i in trackOne.indices) {
            // Numbers borrowed from Stack Overflow Code
            // Needs fine tuning as we don't really understand why the values are what they are
            val sample1 = trackOne[i] / 128.0f * 1.2f
            val sample2 = trackTwo[i] / 250.0f

            // Average the altitude of each sample into one value
            val mixed = (sample1 + sample2) / 2
            // Convert to binaryString
            var mixedString = Integer.toBinaryString(mixed.toInt())

            // Pad each value so they all have 16 bits
            mixedString = mixedString.padStart(16, '0')

            // Trim off overflow bits
            mixedString = mixedString.substring(mixedString.length - 16)

            // Separate 2 byte value into individual bytes
            val byte1 = mixedString.substring(0, 8)
            val byte2 = mixedString.substring(8, 16)

            // Add bytes sequentially to output byte array
            output[outputIndex] = Integer.parseInt(byte1, 2).toByte()
            output[outputIndex + 1] = Integer.parseInt(byte2, 2).toByte()
            outputIndex += 2
        }

        return output
    }

    fun mixAmplitudesEightBit(trackOne: ShortArray, trackTwo: ShortArray): ByteArray {
        // 8 bit file + 8 bit file = 8 bit file
        // Code from: https://stackoverflow.com/questions/40929243/how-to-mix-two-wav-files-without-noise

        val output = ByteArray(if (trackOne.size > trackTwo .size) trackTwo.size else trackTwo.size)

        for (i in output.indices) {
            val samplef1 = trackOne[i] / 128.0f * 1.2f // 2^7=128
            val samplef2 = trackTwo[i] / 128.0f * 1.2f
            val mixed = (samplef1 + samplef2) / 2
            val outputSample = (mixed * 128.0f).toByte()
            output[i] = outputSample
        }

        return output
    }
}
