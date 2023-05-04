origin：[JunzhouLiu/BILIBILI-HELPER](https://github.com/JunzhouLiu/BILIBILI-HELPER)

fork：[OreosLab/bili](https://github.com/OreosLab/bili)

使用：

```
docker run --rm --name bili-helper --restart always -v <your-path>/config.json:/config/config.json ghcr.io/maokwen/bili-helper
```
原本的 README 在[这里](docs/README.old.md)，做了几点修改：

1. Bark 推送
    ```
    "pushConfig": {
        "BARK_KEY": "<your bark token>",
    }
    ```
2. 减小了 Docker 体积 (<100Μ)
