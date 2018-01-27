Compression
===========

Playing around with compression.

bitstream: a library for doing I/O with variable bit-length words
api: the basic API for codecs
rle: simple run length codec
huffman: a huffman tree codec
lzw: an LZW codec
bwt: a Burrows-Wheeler transform
cli: a simple cli to run the the codecs on a file

Notes
-----

On English text, Huffman compresses around 60%, LZW around 50%.  State
of the art (gzip/bzip2/xz/etc.) is ~35%.  Burrows-Wheeler on top of
LZW makes it worse.  Burrows-Wheeler with large block size helps,
seems to work with well with RLE.

TODO
----

- Configurable block sizes
- Reset LZW dictionary at some threshold
- Figure how to use BWT effectively (move-to-front)
- Do BWT without EOF marker
- DEFLATE
- LZMA
