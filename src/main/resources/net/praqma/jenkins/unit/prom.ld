OUTPUT_ARCH ( m68k )

MEMORY
 {
  reserved (!A)  : ORIGIN = 0, LENGTH = 0x00FFFFF
  app (rx) : ORIGIN = 0x100000, LENGTH = 1M
  reserved (!A)  : ORIGIN = 0x0300000, LENGTH = 0x04FFFFF
  ram2 (w)     : ORIGIN = 0x0800000, LENGTH = 2k
 }

SECTIONS {
  . = 0x100000;
  evtsect : {
    _evtsect = .;
    *( evtsect )
  }
  . = 0x100400; 
  crc : {
    *(crc)
  } 
  . = 0x100408; 
  .entrycode : { *(entrycode) }
  .init : { KEEP (*(.init)) }
  .text : {
      
  }

  .strings : { *(.string) }

  . = ALIGN(2);

    _flash_data = .;
  . = 0x0800000 ;
    .data : AT (_flash_data)
    {

    }
  
    .stack : {
      _stack = .;
      stack = .;
    }

    .bootvar : { *(.bootvar) *(bootdata) }
    _vars = .;
    .bss :
    {
        _bss = .;
        *(.bss*)
        *(COMMON)
        _ebss = .;
        _end = .;
    }
   heap : {
     *(heap)
     . += _heapsize;
      _heapend = .;
   }
}