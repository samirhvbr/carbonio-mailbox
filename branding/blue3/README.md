# Blue3 branding defaults

The default identity of this fork is Blue3. The existing Blue3 wordmarks and
symbol are copied unchanged from the active Blue3 visual identity.

| Setting | Default |
| --- | --- |
| Web and admin titles | Blue3 |
| Light logo | `blue3-marca.png` |
| Dark logo | `blue3-marca-branca.png` |
| Favicon | `blue3-simbolo.png` |
| Light primary colour | `#0071bc` |
| Dark primary colour | `#29abe2` |

The backend loads [branding.properties](../../store/src/main/resources/com/blue3/mailbox/branding.properties)
and supplies these fallback values in `GetDomainInfo` responses when configuration
defaults are requested. Explicit domain values and custom global values take
precedence. The schema's built-in Carbonio titles and descriptions are replaced
by the Blue3 defaults. An authenticated request with `applyConfig=false`
returns the stored attributes for administration.

The HTTPS logo URLs point to the files on this fork's `main` branch. Deployments
can set their own HTTPS asset URLs through the existing domain/global branding
attributes to host the images on their own infrastructure.

Carbonio's login, web client and admin console are distributed separately.
Their rendered branding depends on the configuration API used by each client
and requires validation with the deployed client versions.
