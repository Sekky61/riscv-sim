{ linkFarm, fetchgit }:

linkFarm "zig-packages" [
  {
    name = "1220411a8c46d95bbf3b6e2059854bcb3c5159d428814099df5294232b9980517e9c";
    path = fetchgit {
      url = "https://github.com/ikskuh/zig-args";
      rev = "0abdd6947a70e6d8cc83b66228cea614aa856206";
      hash = "sha256-8XsanBOB8hJpWracgKE+zQmDgBjYL871xCh2H3rWTqA=";
    };
  }
]
