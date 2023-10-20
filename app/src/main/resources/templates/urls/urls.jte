@import hexlet.code.dto.urls.UrlsPage
@param UrlsPage page


@template.layout.page(
page = page,
content = @`
    <section>
        <div class="container-lg mt-5">
            <div class="row ">
                <div class="col-3">
                    <h1>Sites</h1>
                </div>
            </div>

            <div class="row m-1">
                <table class="table table-bordered table-hover">
                    <thead>
                    <tr>
                        <th class="col-1">ID</th>
                        <th class="col-8">Name</th>
                        <th class="col-2">Last check</th>
                        <th class="col-1">Response code</th>
                    </tr>
                    </thead>
                    <tbody>
                    @for(var url : page.getUrls())
                    <tr>
                        <td>${url.getId()}</td>
                        <td><a href="/urls/${url.getId()}">${url.getName()}</a></td>
                        <td>Last check</td>
                        <td>Response code</td>
                    </tr>
                    @endfor
                    </tbody>
                </table>
            </div>
        </div>
    </section>
`,
)