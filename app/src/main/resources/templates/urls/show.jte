@import hexlet.code.dto.urls.UrlPage
@param UrlPage page

@template.layout.page(
page = page,
content = @`
<section>
    <div class="container-lg mt-5">
        <h1>Page ${page.getUrl().getName()}</h1>
        <table class="table table-bordered table-hover mt-3">
            <tbody>
            <tr>
                <td>ID</td>
                <td>${page.getUrl().getId()}</td>
            </tr>
            <tr>
                <td>Name</td>
                <td>${page.getUrl().getName()}</td>
            </tr>
            <tr>
                <td>Date</td>
                <td>${page.getUrl().getCreatedAt().toLocalDateTime().toString()}</td>
            </tr>
            </tbody>
        </table>

        <h2 class="mt-5">Checklist</h2>
        <form method="post" action="/urls/${page.getUrl().getId()}/checks}">
            <button type="submit" class="btn btn-primary">Run check</button>
        </form>

        <table class="table table-bordered table-hover mt-3">
            <thead>
            <tr><th class="col-1">ID</th>
                <th class="col-1">Response code</th>
                <th>title</th>
                <th>h1</th>
                <th>description</th>
                <th class="col-2">Date of check</th>
            </tr></thead>
            <tbody>
            <!-- each check -->
            <tr>
                <td>{getId()}</td>
                <td>{getStatusCode()}</td>
                <td>{#strings.abbreviate(getTitle(), 20)}</td>
                <td>{#strings.abbreviate(getH1(), 20)}</td>
                <td>{#strings.abbreviate(getDescription(), 50)}</td>
                <td>{#temporals.format(getCreatedAt(), 'dd/MM/yyyy HH:mm')}</td>
            </tr>
            </tbody>
        </table>
    </div>
</section>
`,
)