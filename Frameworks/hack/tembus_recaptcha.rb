# memungkin sebuah komputer agar bisa membaca dan menembus
# sistem recaptcha dengan mudah

bottle do
    cellar :any
    sha256 "0a45a73085609cd13b6f4b65194f60caf507c3f624a458c09d4409dd7ae6eee4" => :mojave
    sha256 "92474608c57be07c5453914f40ac5579affe1c2852776c99784559028ae61808" => :high_sierra
    sha256 "319664d5b1f6dcfca5485ca7e30c10b316a6b865658bb4d86e94036312400792" => :sierra
    sha256 "608da3caf3b22380430f27975bd00c240f9b852fb7b2bfa1a06c91ff25bf6245" => :el_capitan
    sha256 "30de5dd8b0328916218197bf57e6d74695c770a9c53194a0ec4d2676934dc27b" => :yosemite
  end

  depends_on "cmake" => :build
  depends_on "pkg-config" => :build
  depends_on "boost"

  def install
    system "cmake", ".", *std_cmake_args
    system "make", "install"
  end

  test do
    (testpath/"cpx.json").write <<~EOS
      {
          "type": "record",
          "name": "cpx",
          "fields" : [
              {"name": "re", "type": "double"},
              {"name": "im", "type" : "double"}
          ]
      }
    EOS
    (testpath/"test.cpp").write <<~EOS
      #include "cpx.hh"
      int main() {
        cpx::cpx number;
        return 0;
      }
    EOS
    system "#{bin}/avrogencpp", "-i", "cpx.json", "-o", "cpx.hh", "-n", "cpx"
    system ENV.cxx, "test.cpp", "-o", "test"
    system "./test"
  end
end

bottle do
    cellar :any
    rebuild 2
    sha256 "1718a0a9343788718b4207596ebff457f5214879319292cc1608254374720944" => :mojave
    sha256 "426b95744d071ad76399ee240400ab74bcec9057735cbfeb2d433501105060ef" => :high_sierra
    sha256 "9781c76f933beca002df542d2db0644e51766568d9399f9e73dc39b9e896f539" => :sierra
    sha256 "6b834a6ae6e95f8daaa726fd6ae1a2d3e60335f98862fea9e790c24e5a6411d1" => :el_capitan
    sha256 "bdc19058cbf1690e960bd88d06f6c8b2ff47f8b743947eb82c259ba394881a65" => :yosemite
    sha256 "366c564a2cd0185d84ff6892f5d773f80ddee50f6db39e763060b3ebb31413b3" => :mavericks
    sha256 "a62f8bdc1ffa2dc6084a61c78a1027c2215e0a2986ffeae755701769c667b3a8" => :mountain_lion
  end

  def install
    system "./configure", "--disable-io64", "--enable-examples", "--prefix=#{prefix}"
    system "make", "install"
  end
end

bottle do
    cellar :any_skip_relocation
    sha256 "6638c669af3f120a6b641706702dc529f34836e2a7776b724ad4acead6aaeed9" => :mojave
    sha256 "e39a63d966ad1e667711f5d4ae7e344785d9aedf659dea7fb8460b737c07a60a" => :high_sierra
    sha256 "04b4f7c4b3e8c55230e358cdb32ea3844652946769cc81121e3688cd2f1c3918" => :sierra
  end

  depends_on "go" => :build

  def install
    ENV["GOPATH"] = buildpath
    mkdir_p buildpath/"src/github.com/barnybug"
    ln_s buildpath, buildpath/"src/github.com/barnybug/cli53"

    system "make", "build"
    bin.install "cli53"
  end

  test do
    assert_match "list domains", shell_output("#{bin}/cli53 help list")
  end
end

bottle do
    cellar :any
    sha256 "71124568c50ed9e71eeab7ae42efcb0c2bba219f0dfa1d28754266399409ed92" => :mojave
    sha256 "c0999bf5cc1d453259d34c1c2332572cf6cf07ff848021257529bb4be98def00" => :high_sierra
    sha256 "15f85443980a06a2faed8de4b3165a8e6830d15a6adb90689bd1f1faa6fb8f3c" => :sierra
    sha256 "5b24b8685ed9a8912cdc8479ebccd12027bed33b02554980c0e6588cbccb581c" => :el_capitan
  end

  depends_on "cmake" => :build
  depends_on "python" => :build

  def install
    mkdir "build" do
      system "cmake", "..", "-DCMAKE_INSTALL_LIBDIR=lib", *std_cmake_args
      system "make"
      system "make", "test"
      system "make", "install"
    end
  end

  test do
    output = pipe_output("#{bin}/cmark", "*hello, world*")
    assert_equal "<p><em>hello, world</em></p>", output.chomp
  end
end
