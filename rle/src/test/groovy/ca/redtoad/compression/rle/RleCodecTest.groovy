package ca.redtoad.compression.rle

import spock.lang.Unroll
import spock.lang.Specification

class RleCodecTest extends Specification {

    @Unroll
    def 'known string "#original" compresses and uncompresses back to the same string'() {
    given:
        def codec = new RleCodec()

    when:
        def compressed = new ByteArrayOutputStream()
        codec.encode(new ByteArrayInputStream(original.bytes), compressed)
        def uncompressedStream = new ByteArrayOutputStream()
        codec.decode(new ByteArrayInputStream(compressed.toByteArray()), uncompressedStream)
        def uncompressed = uncompressedStream.toString()

    then:
        uncompressed == original

    where:
        original                       | _
        ''                             | _
        'aaaaaaaaaaaaaaaaaaaaaaaaaaaa' | _
        'baaaaaaaaaaaaaaaaaaaaaaaaaaa' | _
        'test string for compression!' | _
        '¯\\_(ツ)_/¯'                  | _
    }
}
